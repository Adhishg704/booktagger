package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.client.BookMetadata;
import com.nemo.booktagger.dao.repository.BookRepository;
import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.dao.repository.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.service.BookService;
import com.nemo.booktagger.service.GoogleBooksService;
import jakarta.transaction.Transactional;
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
        return byTitleAndAuthor.orElseGet(() -> addBook(title, author, isbn));
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
}
