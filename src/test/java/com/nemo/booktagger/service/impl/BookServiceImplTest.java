package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.client.BookMetadata;
import com.nemo.booktagger.dao.repository.BookRepository;
import com.nemo.booktagger.dao.repository.BookTagRepository;
import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.dao.repository.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.factory.BookFactory;
import com.nemo.booktagger.factory.UserFactory;
import com.nemo.booktagger.service.GoogleBooksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {
    private final String bookAlreadyExistsExceptionMessage = "Book already exists";
    private User user;
    private Book book;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private BookTagRepository bookTagRepository;

    @Mock
    private GoogleBooksService googleBooksService;

    @InjectMocks
    private BookServiceImpl bookService;

    @BeforeEach
    public void setUp() {
        user = UserFactory.createUser("user1", "user1@gmail.com");
        book = BookFactory.createBook("Book1", "Author1", "Desc1", "111111", "2025");
    }

    @Test
    public void testAddUserBookThrowsExceptionForExistingUserBook() {
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);
        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        when(userBookRepository.existsByUser_IdAndBook_Id(user.getId(), book.getId())).thenReturn(true);

        RuntimeException exc = assertThrows(
                RuntimeException.class,
                () -> bookService.addUserBook(user.getId(), book.getId(), "2025", ReadingStatus.READ, 5.0)
        );

        String expectedExceptionMessage = "Book already in user's library";
        assertEquals(expectedExceptionMessage, exc.getMessage(), "Unexpected exception message");
        verify(userRepository, times(1)).getReferenceById(eq(user.getId()));
        verify(bookRepository, times(1)).getReferenceById(eq(book.getId()));
        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    public void testAddBookThrowsExceptionForExistingBook() {
        String title = "Title";
        String author = "author";
        String isbn = "1234567891";
        when(bookRepository.existsByIsbn(eq(isbn))).thenReturn(true);

        RuntimeException runtimeException = assertThrows(
                RuntimeException.class,
                () -> bookService.addBook(title, author, isbn)
        );

        assertEquals(bookAlreadyExistsExceptionMessage, runtimeException.getMessage(), "Unexpected exception " +
                "message");
        verify(bookRepository, times(1)).existsByIsbn(eq(isbn));
        verify(bookRepository, never()).save(any());
        verify(googleBooksService, never()).getBookMetadataFromGoogleBooks(any(), any(), any());
    }

    @Test
    public void testAddBookAddsBookForNonExistingBookExistingBookMetadata() {
        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setIsbn("1234567891");
        BookMetadata bookMetadata = new BookMetadata("Description", "2025");
        when(googleBooksService.getBookMetadataFromGoogleBooks(eq(book.getIsbn()), eq(book.getTitle()),
                eq(book.getAuthor()))).thenReturn(Optional.of(bookMetadata));
        when(bookRepository.existsByIsbn(eq(book.getIsbn()))).thenReturn(false);
        when(bookRepository.existsByTitleAndAuthor(eq(book.getTitle()), eq(book.getAuthor()))).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book addedBook = bookService.addBook(book.getTitle(), book.getAuthor(), book.getIsbn());

        assertNotNull(addedBook, "Book should be returned");
        assertEquals(book.getTitle(), addedBook.getTitle(), "Unexpected book title");
        assertEquals(book.getAuthor(), addedBook.getAuthor(), "Unexpected book author");
        assertEquals(book.getIsbn(), addedBook.getIsbn(), "Unexpected book isbn");
        assertEquals(bookMetadata.getDescription(), addedBook.getDescription(), "Unexpected book description");
        assertEquals(bookMetadata.getPublishedDate(), addedBook.getYearPublished(), "Unexpected published date");
        verify(bookRepository, times(1)).existsByIsbn(eq(book.getIsbn()));
        verify(bookRepository, times(1)).existsByTitleAndAuthor(eq(book.getTitle()),
                eq(book.getAuthor()));
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(googleBooksService, times(1)).getBookMetadataFromGoogleBooks(eq(book.getIsbn()), eq(book.getTitle()),
                eq(book.getAuthor()));
    }

    @Test
    public void testAddBookAddsBookForNonExistingBookNonExistingBookMetadata() {
        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setIsbn("1234567891");
        when(googleBooksService.getBookMetadataFromGoogleBooks(eq(book.getIsbn()), eq(book.getTitle()),
                eq(book.getAuthor()))).thenReturn(Optional.empty());
        when(bookRepository.existsByIsbn(eq(book.getIsbn()))).thenReturn(false);
        when(bookRepository.existsByTitleAndAuthor(eq(book.getTitle()), eq(book.getAuthor()))).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book addedBook = bookService.addBook(book.getTitle(), book.getAuthor(), book.getIsbn());

        assertNotNull(addedBook, "Book should be returned");
        assertEquals(book.getTitle(), addedBook.getTitle(), "Unexpected book title");
        assertEquals(book.getAuthor(), addedBook.getAuthor(), "Unexpected book author");
        assertEquals(book.getIsbn(), addedBook.getIsbn(), "Unexpected book isbn");
        assertEquals("", addedBook.getDescription(), "Book description not available");
        assertEquals("", addedBook.getYearPublished(), "Book published date not available");
        verify(bookRepository, times(1)).existsByIsbn(eq(book.getIsbn()));
        verify(bookRepository, times(1)).existsByTitleAndAuthor(eq(book.getTitle()),
                eq(book.getAuthor()));
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(googleBooksService, times(1)).getBookMetadataFromGoogleBooks(eq(book.getIsbn()), eq(book.getTitle()),
                eq(book.getAuthor()));
    }

    @Test
    public void testAddUserBookCreatesUserBookForNonExistingUserBook() {
        UserBook userBook = new UserBook();
        userBook.setUser(user);
        userBook.setBook(book);
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);
        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        when(userBookRepository.existsByUser_IdAndBook_Id(user.getId(), book.getId())).thenReturn(false);
        when(userBookRepository.save(any(UserBook.class))).thenReturn(userBook);

        UserBook returnedUserBook = bookService.addUserBook(user.getId(), book.getId(), "2025", ReadingStatus.READ, 5.0);

        assertNotNull(returnedUserBook, "User book should be returned");
        assertEquals(userBook.getUser(), returnedUserBook.getUser(), "Unexpected user");
        assertEquals(userBook.getBook(), returnedUserBook.getBook(), "Unexpected book");
        verify(userRepository, times(1)).getReferenceById(eq(user.getId()));
        verify(bookRepository, times(1)).getReferenceById(eq(book.getId()));
        verify(userBookRepository, times(1)).save(any(UserBook.class));
    }
}