package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.client.BookMetadata;
import com.nemo.booktagger.dao.repository.BookRepository;
import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.dao.repository.UserRepository;
import com.nemo.booktagger.dao.specification.UserBookSpecifications;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.rest.dto.request.UserBookFilterRequest;
import com.nemo.booktagger.rest.dto.response.UserBookResponse;
import com.nemo.booktagger.service.BookService;
import com.nemo.booktagger.service.GoogleBooksService;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;

    private final GoogleBooksService googleBooksService;

    public BookServiceImpl(UserRepository userRepository, BookRepository bookRepository, UserBookRepository userBookRepository,
                           GoogleBooksService googleBooksService) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.userBookRepository = userBookRepository;
        this.googleBooksService = googleBooksService;
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
        Optional<BookMetadata> bookMetadata = googleBooksService.
                getBookMetadataFromGoogleBooks(isbn, title, author);

        String description = null;
        String yearPublished = null;
        String thumbnailURL = null;

        if(bookMetadata.isPresent()) {
            description = bookMetadata.get().getDescription();
            thumbnailURL = bookMetadata.get().getThumbnailURL();
            String date = bookMetadata.get().getPublishedDate();

            if(date != null && date.length() >= 4) {
                yearPublished = date.substring(0, 4);
            }
        }

        Book book = new Book(
                title,
                author,
                description,
                isbn,
                yearPublished,
                thumbnailURL
        );
        bookRepository.save(book);
        return book;
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
        if(byTitleAndAuthor.isPresent()) {
            return byTitleAndAuthor.get();
        }

        return addBook(title, author, isbn);
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
    public List<UserBookResponse> searchUserBooks(Integer userId, UserBookFilterRequest filter) {
        Specification<UserBook> spec =
                Specification.where(UserBookSpecifications.hasUser(userId));

        if(filter.getYearRead() != null) {
            spec = spec.and(UserBookSpecifications.hasYearRead(filter.getYearRead()));
        }

        if(filter.getYearPublished() != null) {
            spec = spec.and(UserBookSpecifications.hasYearPublished((filter.getYearPublished())));
        }

        if(filter.getStatus() != null) {
            spec = spec.and(UserBookSpecifications.hasStatus(filter.getStatus()));
        }

        List<UserBook> userBooks = userBookRepository.findAll(spec);

        return userBooks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private UserBookResponse mapToResponse(UserBook userBook) {
        Book book = userBook.getBook();

        return new UserBookResponse(
                book.getTitle(),
                book.getAuthor(),
                book.getYearPublished(),
                userBook.getYearRead(),
                userBook.getStatus(),
                userBook.getRating()
        );
    }
}
