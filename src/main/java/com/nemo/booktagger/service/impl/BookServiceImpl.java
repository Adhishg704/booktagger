package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.client.BookMetadata;
import com.nemo.booktagger.dao.BookRepository;
import com.nemo.booktagger.dao.BookTagRepository;
import com.nemo.booktagger.dao.UserBookRepository;
import com.nemo.booktagger.dao.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.service.BookService;
import com.nemo.booktagger.service.GoogleBooksService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final BookTagRepository bookTagRepository;

    private final GoogleBooksService googleBooksService;

    public BookServiceImpl(UserRepository userRepository, BookRepository bookRepository, UserBookRepository userBookRepository,
                           BookTagRepository bookTagRepository, GoogleBooksService googleBooksService) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.userBookRepository = userBookRepository;
        this.bookTagRepository = bookTagRepository;
        this.googleBooksService = googleBooksService;
    }

    @Override
    public Book getBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> bookNotFound(bookId));
    }

    @Override
    public String getTitleById(Integer bookId) {
        return bookRepository.findTitleById(bookId)
                .orElseThrow(() -> bookNotFound(bookId));
    }

    @Override
    public String getAuthorById(Integer bookId) {
        return bookRepository.findAuthorById(bookId)
                .orElseThrow(() -> bookNotFound(bookId));
    }

    @Override
    public String getDescriptionById(Integer bookId) {
        return bookRepository.findDescriptionById(bookId)
                .orElseThrow(() -> bookNotFound(bookId));
    }

    @Override
    public String getIsbnById(Integer bookId) {
        return bookRepository.findIsbnById(bookId)
                .orElseThrow(() -> bookNotFound(bookId));
    }

    @Override
    public String getYearPublishedById(Integer bookId) {
        return bookRepository.findYearPublishedById(bookId)
                .orElseThrow(() -> bookNotFound(bookId));
    }

    @Override
    @Transactional
    public Book addBook(String title, String author, String isbn) {
        if((isbn != null && !isbn.isBlank() && bookRepository.existsByIsbn(isbn)) ||
            bookRepository.existsByTitleAndAuthor(title, author)) {
            throw new RuntimeException("Book already exists");
        }
        Optional<BookMetadata> bookMetadata = googleBooksService.
                getBookMetadataFromGoogleBooks(isbn, title, author);

        String description = "";
        String yearPublished = "";

        if(bookMetadata.isPresent()) {
            description = bookMetadata.get().getDescription();
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
                yearPublished
        );
        bookRepository.save(book);
        return book;
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
    public long getNumberOfUsersWhoOwnTheBook(Integer bookId) {
        return userBookRepository.countByBook_Id(bookId);
    }

    @Override
    public List<BookTag> getTagsAssociatedWithBookForUser(Integer userId, Integer bookId) {
        return bookTagRepository.findByUser_IdAndBook_Id(userId, bookId);
    }

    @Override
    public List<UserBook> getUserBooksByAuthor(Integer userId, String author) {
        return userBookRepository.findByUser_IdAndBook_Author(userId, author);
    }

    @Override
    public List<UserBook> getUserBooksByTitle(Integer userId, String titlePart) {
        return List.of();
    }

    @Override
    public List<UserBook> getUserBooksByYearPublished(Integer userId, String year) {
        return userBookRepository.findByUser_IdAndBook_YearPublished(userId, year);
    }

    @Override
    public List<UserBook> getUserBooksByYearRead(Integer userId, String year) {
        return userBookRepository.findByUser_IdAndYearRead(userId, year);
    }

    @Override
    public List<UserBook> getUserBooksByRatingRange(Integer userId, double minRating, double maxRating) {
        return List.of();
    }

    @Override
    public List<UserBook> getBooksReadByUser(Integer userId) {
        return userBookRepository.findByUser_IdAndStatus(userId, ReadingStatus.READ);
    }

    @Override
    public List<UserBook> getBooksUserDidNotFinish(Integer userId) {
        return userBookRepository.findByUser_IdAndStatus(userId, ReadingStatus.DNF);
    }

    @Override
    public List<UserBook> getBooksUserWantsToRead(Integer userId) {
        return userBookRepository.findByUser_IdAndStatus(userId, ReadingStatus.TO_READ);
    }

    @Override
    public UserBook updateUserBookStatus(Integer userId, Integer bookId, ReadingStatus status) {
        return null;
    }

    @Override
    public UserBook updateUserBookRating(Integer userId, Integer bookId, double rating) {
        return null;
    }

    @Override
    public void deleteUserBook(Integer userId, Integer bookId) {

    }

    private EntityNotFoundException bookNotFound(Integer bookId) {
        return new EntityNotFoundException("Book id " + bookId + " not found");
    }
}
