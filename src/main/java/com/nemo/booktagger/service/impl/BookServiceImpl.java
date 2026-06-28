package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.client.BookMetadata;
import com.nemo.booktagger.client.BookMetadataProvider;
import com.nemo.booktagger.dao.repository.BookRepository;
import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.dao.repository.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.event.EnrichBookEvent;
import com.nemo.booktagger.rest.dto.response.ai.EnrichedBook;
import com.nemo.booktagger.service.BookService;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final BookMetadataProvider bookMetadataProvider;
    private final KafkaTemplate<String, EnrichBookEvent> kafkaEnrichTemplate;

    public BookServiceImpl(UserRepository userRepository, BookRepository bookRepository, UserBookRepository userBookRepository,
                           BookMetadataProvider bookMetadataProvider, KafkaTemplate<String, EnrichBookEvent> enrichBookEventKafkaTemplate) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.userBookRepository = userBookRepository;
        this.bookMetadataProvider = bookMetadataProvider;
        kafkaEnrichTemplate = enrichBookEventKafkaTemplate;
    }

    @Override
    @Transactional
    public Book addBook(String title, String author, String isbn) {
        if(isbn == null || isbn.isBlank()) {
            isbn = null;
        }
        if((isbn != null && !isbn.isBlank() && bookRepository.existsByIsbn(isbn)) ||
            bookRepository.existsByTitleAndAuthor(title, author)) {
            throw new RuntimeException("Book already exists");
        }

        Book book = new Book(
                title,
                author,
                null,
                isbn,
                null,
                null
        );

        bookRepository.save(book);

        kafkaEnrichTemplate.send("enrich-book", new EnrichBookEvent(book.getId()));

        return book;
    }

    @Override
    @Transactional
    @KafkaListener(topics = "enrich-book")
    public void enrichBook(EnrichBookEvent enrichBookEvent) {
        Book book = getBookById(enrichBookEvent.bookId());
        BookMetadata bookMetadata = fetchBookMetadata(book.getTitle(), book.getAuthor(), book.getIsbn());

        book.setDescription(bookMetadata.getDescription());
        book.setThumbnailURL(bookMetadata.getThumbnailURL());
        book.setYearPublished(bookMetadata.getPublishedDate());
    }

    private BookMetadata fetchBookMetadata(String title, String author, String isbn) {
        return bookMetadataProvider.search(isbn, title, author).map(
                result -> {
                    String desc = bookMetadataProvider.fetchDescription(result.providerId()).orElse(null);
                    return new BookMetadata(
                            desc,
                            result.yearPublished() != null? Integer.toString(result.yearPublished()): null,
                            result.coverURL()
                    );
                }
        ).orElse(new BookMetadata(null, null, null));
    }

    @Transactional
    @Override
    public Book getOrCreateBook(String title, String author, String isbn) {
        if((isbn != null)) {
            Optional<Book> byIsbn = bookRepository.findByIsbn(isbn);
            if(byIsbn.isPresent()) {
                return byIsbn.get();
            }
         }

        Optional<Book> byTitleAndAuthor = bookRepository.findByTitleAndAuthor(title, author);
        return byTitleAndAuthor.orElseGet(() -> addBook(title, author, isbn));
    }

    @Override
    public Book getBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @Override
    public Book getBookReferenceById(Integer bookId) {
        return bookRepository.getReferenceById(bookId);
    }

    @Override
    @Transactional
    public UserBook addUserBook(Integer userId, Integer bookId, String yearRead, ReadingStatus status, Double rating) {
        User user = userRepository.getReferenceById(userId);
        Book book = bookRepository.getReferenceById(bookId);

        if(userBookRepository.existsByUser_IdAndBook_Id(userId, bookId)) {
            throw new RuntimeException("Book already in user's library");
        }

        UserBook userBook = new UserBook();
        userBook.setUser(user);
        userBook.setBook(book);
        userBook.setYearRead(yearRead);
        userBook.setStatus(status);
        userBook.setRating(rating);

        return userBookRepository.save(userBook);
    }

    @Override
    @Transactional
    public void embedBook(EnrichedBook enrichedBook, float[] vector) {
        Book dbBook = bookRepository.findById(enrichedBook.bookId())
                .orElseThrow();
        dbBook.setEmbedding(vector);

        bookRepository.save(dbBook);
    }

    @Override
    public List<Integer> getSimilarBooksFromLibrary(Integer userId, float[] userQueryEmbedded) {
        return bookRepository.retrieveBookIdsSimilarToUserQuery(userId, userQueryEmbedded);
    }

    @Override
    public boolean isBookAlreadySavedForUser(Integer userId, String isbn) {
        return userBookRepository.existsByUser_IdAndBook_Isbn(userId, isbn);
    }
}
