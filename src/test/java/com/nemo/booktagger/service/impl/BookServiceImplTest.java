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
import com.nemo.booktagger.factory.UserBookFactory;
import com.nemo.booktagger.factory.UserFactory;
import com.nemo.booktagger.rest.dto.request.UserBookFilterRequest;
import com.nemo.booktagger.rest.dto.response.UserBookResponse;
import com.nemo.booktagger.service.GoogleBooksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
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
        book = BookFactory.createBook("Book1", "Author1", "Desc1", "111111", "2025", "thumbnail");
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
        BookMetadata bookMetadata = new BookMetadata("Description", "2025", "Thumbnail");
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
        assertEquals(bookMetadata.getThumbnailURL(), addedBook.getThumbnailURL(), "Unexpected thumbnail URL");
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
        assertNull(addedBook.getDescription(), "Book description not available");
        assertNull(addedBook.getYearPublished(), "Book published date not available");
        assertNull(addedBook.getThumbnailURL(), "Book image URL not available");
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

    @Test
    public void testSearchUserBooksByYearReadReturnsCorrectResponseForExistingYearRead() {
        String yearRead = "2025";
        UserBook userBook = UserBookFactory.createUserBook(
                user, book, yearRead, ReadingStatus.READ, 5.0);
        List<UserBook> userBookList = List.of(userBook);
        UserBookFilterRequest request =
                new UserBookFilterRequest(yearRead, null, null);
        when(userBookRepository.findAll(any(Specification.class))).thenReturn(userBookList);

        List<UserBookResponse> response =
                bookService.searchUserBooks(user.getId(), request);

        verify(userBookRepository).findAll(any(Specification.class));
        assertNotNull(response);
        assertEquals(userBookList.size(), response.size());
        assertEquals(yearRead, response.getFirst().getYearRead());
    }

    @Test
    public void testSearchUserBooksByYearReadReturnsCorrectResponseForNonExistingYearRead() {
        String yearRead = "2024";

        UserBookFilterRequest request =
                new UserBookFilterRequest(yearRead, null, null);

        when(userBookRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<UserBookResponse> response =
                bookService.searchUserBooks(user.getId(), request);

        verify(userBookRepository).findAll(any(Specification.class));
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    public void testSearchUserBooksByYearPublishedReturnsCorrectResponseForExistingYearPublished() {
        String yearPublished = "2025";
        book.setYearPublished(yearPublished);
        UserBook userBook = UserBookFactory.createUserBook(
                user, book, "2025", ReadingStatus.READ, 5.0);
        List<UserBook> userBookList = List.of(userBook);
        UserBookFilterRequest request =
                new UserBookFilterRequest(null, yearPublished, null);
        when(userBookRepository.findAll(any(Specification.class)))
                .thenReturn(userBookList);

        List<UserBookResponse> response =
                bookService.searchUserBooks(user.getId(), request);

        verify(userBookRepository).findAll(any(Specification.class));
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(yearPublished, response.getFirst().getYearPublished());
    }

    @Test
    public void testSearchUserBooksByYearPublishedReturnsEmptyForNonExistingYearPublished() {
        String yearPublished = "1999";
        UserBookFilterRequest request =
                new UserBookFilterRequest(null, yearPublished, null);
        when(userBookRepository.findAll(any(Specification.class)))
                .thenReturn(List.of());

        List<UserBookResponse> response =
                bookService.searchUserBooks(user.getId(), request);

        verify(userBookRepository).findAll(any(Specification.class));
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    public void testSearchUserBooksByStatusReturnsCorrectResponseForExistingStatus() {
        ReadingStatus status = ReadingStatus.READ;
        UserBook userBook = UserBookFactory.createUserBook(
                user, book, "2025", status, 5.0);
        List<UserBook> userBookList = List.of(userBook);
        UserBookFilterRequest request =
                new UserBookFilterRequest(null, null, status);
        when(userBookRepository.findAll(any(Specification.class)))
                .thenReturn(userBookList);

        List<UserBookResponse> response =
                bookService.searchUserBooks(user.getId(), request);

        verify(userBookRepository).findAll(any(Specification.class));
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(status, response.getFirst().getStatus());
    }
}