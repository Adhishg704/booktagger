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
import com.nemo.booktagger.factory.BookFactory;
import com.nemo.booktagger.factory.UserFactory;
import com.nemo.booktagger.service.GoogleBooksService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {
    private final Integer existingBookId = 1;
    private final Integer nonExistingBookId = 2;
    private final String bookNotFoundExceptionMessage = "Book id " + nonExistingBookId + " not found";
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
    public void testGetBookByIdReturnsBookForExistingBook() {
        when(bookRepository.findById(existingBookId)).thenReturn(Optional.of(book));

        Book returnedBook = bookService.getBookById(existingBookId);

        assertNotNull(returnedBook);
        assertEquals(book.getTitle(), returnedBook.getTitle(), "Book titles must match");
        assertEquals(book.getAuthor(), returnedBook.getAuthor(), "Authors must match");
        assertEquals(book.getIsbn(), returnedBook.getIsbn(), "ISBN should match");
        verify(bookRepository, times(1)).findById(existingBookId);
    }

    @Test
    public void testGetBookByIdThrowsExceptionForNonExistingBook() {
        when(bookRepository.findById(nonExistingBookId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.getBookById(nonExistingBookId)
        );

        assertEquals(bookNotFoundExceptionMessage, ex.getMessage(), "Unexcpected exception message");
        verify(bookRepository, times(1)).findById(nonExistingBookId);
    }

    @Test
    public void testGetTitleByIdReturnsTitleForExistingBook() {
        when(bookRepository.findTitleById(existingBookId))
                .thenReturn(Optional.of(book.getTitle()));

        String author = bookService.getTitleById(existingBookId);

        assertEquals(book.getTitle(), author);
        verify(bookRepository, times(1)).findTitleById(existingBookId);
    }

    @Test
    public void testGetTitleByIdThrowsExceptionForNonExistingBook() {
        when(bookRepository.findTitleById(nonExistingBookId))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.getTitleById(nonExistingBookId)
        );

        assertEquals("Book id " + nonExistingBookId + " not found", ex.getMessage());
        verify(bookRepository, times(1)).findTitleById(nonExistingBookId);
    }

    @Test
    public void testGetAuthorByIdReturnsAuthorForExistingBook() {
        when(bookRepository.findAuthorById(existingBookId))
                .thenReturn(Optional.of(book.getAuthor()));

        String author = bookService.getAuthorById(existingBookId);

        assertEquals(book.getAuthor(), author);
        verify(bookRepository, times(1)).findAuthorById(existingBookId);
    }

    @Test
    public void testGetAuthorByIdThrowsExceptionForNonExistingBook() {
        when(bookRepository.findAuthorById(nonExistingBookId))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.getAuthorById(nonExistingBookId)
        );

        assertEquals("Book id " + nonExistingBookId + " not found", ex.getMessage());
        verify(bookRepository, times(1)).findAuthorById(nonExistingBookId);
    }

    @Test
    public void testGetDescriptionByIdReturnsDescriptionForExistingBook() {
        when(bookRepository.findDescriptionById(existingBookId))
                .thenReturn(Optional.of(book.getDescription()));

        String description = bookService.getDescriptionById(existingBookId);

        assertEquals(book.getDescription(), description);
        verify(bookRepository, times(1)).findDescriptionById(existingBookId);
    }

    @Test
    public void testGetDescriptionByIdThrowsExceptionForNonExistingBook() {
        when(bookRepository.findDescriptionById(nonExistingBookId))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.getDescriptionById(nonExistingBookId)
        );

        assertEquals("Book id " + nonExistingBookId + " not found", ex.getMessage());
        verify(bookRepository, times(1)).findDescriptionById(nonExistingBookId);
    }

    @Test
    public void testGetYearPublishedByIdReturnsYearForExistingBook() {
        when(bookRepository.findYearPublishedById(existingBookId))
                .thenReturn(Optional.of(book.getYearPublished()));

        String year = bookService.getYearPublishedById(existingBookId);

        assertEquals(book.getYearPublished(), year);
        verify(bookRepository, times(1)).findYearPublishedById(existingBookId);
    }

    @Test
    public void testGetYearPublishedByIdThrowsExceptionForNonExistingBook() {
        when(bookRepository.findYearPublishedById(nonExistingBookId))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.getYearPublishedById(nonExistingBookId)
        );

        assertEquals("Book id " + nonExistingBookId + " not found", ex.getMessage());
        verify(bookRepository, times(1)).findYearPublishedById(nonExistingBookId);
    }

    @Test
    public void testGetIsbnByIdReturnsIsbnForExistingBook() {
        when(bookRepository.findIsbnById(existingBookId))
                .thenReturn(Optional.of(book.getIsbn()));

        String isbn = bookService.getIsbnById(existingBookId);

        assertEquals(book.getIsbn(), isbn);
        verify(bookRepository, times(1)).findIsbnById(existingBookId);
    }

    @Test
    public void testGetIsbnByIdThrowsExceptionForNonExistingBook() {
        when(bookRepository.findIsbnById(nonExistingBookId))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.getIsbnById(nonExistingBookId)
        );

        assertEquals("Book id " + nonExistingBookId + " not found", ex.getMessage());
        verify(bookRepository, times(1)).findIsbnById(nonExistingBookId);
    }

    @Test
    public void testGetNumberOfUsersWhoOwnTheBookReturnsCorrectValue() {
        long expectedUsers = 5L;
        when(userBookRepository.countByBook_Id(existingBookId)).thenReturn(expectedUsers);

        long usersWhoOwnTheBook = bookService.getNumberOfUsersWhoOwnTheBook(existingBookId);

        assertEquals(expectedUsers, usersWhoOwnTheBook, "Number of users who own the book is returned incorrectly");
        verify(userBookRepository, times(1)).countByBook_Id(existingBookId);
    }

    @Test
    public void testGetTagsAssociatedWithBookForUserReturnsTags() {
        Integer userId = 10;
        Integer bookId = existingBookId;

        List<BookTag> expectedTags = List.of(
                new BookTag(),
                new BookTag()
        );

        when(bookTagRepository.findByUser_IdAndBook_Id(userId, bookId))
                .thenReturn(expectedTags);

        List<BookTag> result =
                bookService.getTagsAssociatedWithBookForUser(userId, bookId);

        assertNotNull(result);
        assertEquals(expectedTags, result, "Returned tags list is incorrect");
        verify(bookTagRepository, times(1))
                .findByUser_IdAndBook_Id(userId, bookId);
    }

    @Test
    public void testGetUserBooksByYearPublished() {
        Integer userId = 10;
        String yearPublished = "2025";

        List<UserBook> expectedUserBooks = List.of(
                new UserBook(),
                new UserBook()
        );

        when(userBookRepository.findByUser_IdAndBook_YearPublished(userId, yearPublished)).thenReturn(expectedUserBooks);

        List<UserBook> returnedUserBooks = bookService.getUserBooksByYearPublished(userId, yearPublished);

        assertEquals(expectedUserBooks, returnedUserBooks, "List of user books is correct");
        verify(userBookRepository, times(1)).findByUser_IdAndBook_YearPublished(userId, yearPublished);
    }

    @Test
    public void testGetUserBooksByYearReadReturnsCorrectList() {
        Integer userId = 10;
        String yearRead = "2024";

        List<UserBook> expectedUserBooks = List.of(
                new UserBook(),
                new UserBook()
        );

        when(userBookRepository.findByUser_IdAndYearRead(userId, yearRead))
                .thenReturn(expectedUserBooks);

        List<UserBook> returnedUserBooks =
                bookService.getUserBooksByYearRead(userId, yearRead);

        assertEquals(expectedUserBooks, returnedUserBooks,
                "List of user books read in a year is incorrect");

        verify(userBookRepository, times(1))
                .findByUser_IdAndYearRead(userId, yearRead);
    }

    @Test
    public void testGetBooksReadByUserReturnsCorrectList() {
        Integer userId = 10;

        List<UserBook> expectedUserBooks = List.of(
                new UserBook(),
                new UserBook()
        );

        when(userBookRepository.findByUser_IdAndStatus(userId, ReadingStatus.READ))
                .thenReturn(expectedUserBooks);

        List<UserBook> returnedUserBooks =
                bookService.getBooksReadByUser(userId);

        assertEquals(expectedUserBooks, returnedUserBooks,
                "List of books read by user is incorrect");

        verify(userBookRepository, times(1))
                .findByUser_IdAndStatus(userId, ReadingStatus.READ);
    }

    @Test
    public void testGetBooksUserDidNotFinishReturnsCorrectList() {
        Integer userId = 10;

        List<UserBook> expectedUserBooks = List.of(
                new UserBook(),
                new UserBook()
        );

        when(userBookRepository.findByUser_IdAndStatus(userId, ReadingStatus.DNF))
                .thenReturn(expectedUserBooks);

        List<UserBook> returnedUserBooks =
                bookService.getBooksUserDidNotFinish(userId);

        assertEquals(expectedUserBooks, returnedUserBooks,
                "List of books user did not finish is incorrect");

        verify(userBookRepository, times(1))
                .findByUser_IdAndStatus(userId, ReadingStatus.DNF);
    }

    @Test
    public void testGetBooksUserWantsToReadReturnsCorrectList() {
        Integer userId = 10;

        List<UserBook> expectedUserBooks = List.of(
                new UserBook(),
                new UserBook()
        );

        when(userBookRepository.findByUser_IdAndStatus(userId, ReadingStatus.TO_READ))
                .thenReturn(expectedUserBooks);

        List<UserBook> returnedUserBooks =
                bookService.getBooksUserWantsToRead(userId);

        assertEquals(expectedUserBooks, returnedUserBooks,
                "List of books user wants to read is incorrect");

        verify(userBookRepository, times(1))
                .findByUser_IdAndStatus(userId, ReadingStatus.TO_READ);
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