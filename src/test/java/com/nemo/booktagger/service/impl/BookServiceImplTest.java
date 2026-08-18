package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.client.BookMetadataProvider;
import com.nemo.booktagger.client.ProviderSearchResult;
import com.nemo.booktagger.dao.repository.BookRepository;
import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.dao.repository.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.event.EnrichBookEvent;
import com.nemo.booktagger.exception.DuplicateResourceException;
import com.nemo.booktagger.exception.ResourceNotFoundException;
import com.nemo.booktagger.factory.BookFactory;
import com.nemo.booktagger.factory.UserFactory;
import com.nemo.booktagger.rest.dto.response.ai.EnrichedBook;
import com.nemo.booktagger.service.UserLibraryCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    private BookMetadataProvider bookMetadataProvider;

    @Mock
    private KafkaTemplate<String, EnrichBookEvent> kafkaEnrichTemplate;

    @Mock
    private UserLibraryCacheService userLibraryCacheService;

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

        DuplicateResourceException exc = assertThrows(
                DuplicateResourceException.class,
                () -> bookService.addUserBook(user.getId(), book.getId(), "2025", ReadingStatus.READ, 5.0)
        );

        String expectedExceptionMessage = "Book already in user's library";
        assertEquals(expectedExceptionMessage, exc.getMessage(), "Unexpected exception message");
        verify(userRepository, times(1)).getReferenceById(eq(user.getId()));
        verify(bookRepository, times(1)).getReferenceById(eq(book.getId()));
        verify(userBookRepository, never()).save(any(UserBook.class));
        verifyNoInteractions(kafkaEnrichTemplate);
    }

    @Test
    public void testAddBookThrowsExceptionForExistingBook() {
        String title = "Title";
        String author = "author";
        String isbn = "1234567891";
        when(bookRepository.existsByIsbn(eq(isbn))).thenReturn(true);

        DuplicateResourceException duplicateResourceException = assertThrows(
                DuplicateResourceException.class,
                () -> bookService.addBook(title, author, isbn)
        );

        assertEquals(bookAlreadyExistsExceptionMessage, duplicateResourceException.getMessage(), "Unexpected exception " +
                "message");
        verify(bookRepository, times(1)).existsByIsbn(eq(isbn));
        verify(bookRepository, never()).save(any());
    }

    @Test
    public void testAddBookThrowsExceptionForExistingTitleAndAuthorWhenIsbnBlank() {
        String title = "Title";
        String author = "author";
        when(bookRepository.existsByTitleAndAuthor(eq(title), eq(author))).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> bookService.addBook(title, author, "  ")
        );

        verify(bookRepository, never()).existsByIsbn(any());
        verify(bookRepository, times(1)).existsByTitleAndAuthor(eq(title), eq(author));
        verify(bookRepository, never()).save(any());
    }

    @Test
    public void testAddBookCreatesBookWithoutMetadataForNonExistingBook() {
        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setIsbn("1234567891");
        when(bookRepository.existsByIsbn(eq(book.getIsbn()))).thenReturn(false);
        when(bookRepository.existsByTitleAndAuthor(eq(book.getTitle()), eq(book.getAuthor()))).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book addedBook = bookService.addBook(book.getTitle(), book.getAuthor(), book.getIsbn());

        assertNotNull(addedBook, "Book should be returned");
        assertEquals(book.getTitle(), addedBook.getTitle(), "Unexpected book title");
        assertEquals(book.getAuthor(), addedBook.getAuthor(), "Unexpected book author");
        assertEquals(book.getIsbn(), addedBook.getIsbn(), "Unexpected book isbn");
        assertNull(addedBook.getDescription(), "Book description not available until enrichment runs");
        assertNull(addedBook.getYearPublished(), "Book published date not available until enrichment runs");
        assertNull(addedBook.getThumbnailURL(), "Book image URL not available until enrichment runs");
        verify(bookRepository, times(1)).existsByIsbn(eq(book.getIsbn()));
        verify(bookRepository, times(1)).existsByTitleAndAuthor(eq(book.getTitle()),
                eq(book.getAuthor()));
        verify(bookRepository, times(1)).save(any(Book.class));
        verifyNoInteractions(bookMetadataProvider);
    }

    @Test
    public void testAddUserBookCreatesUserBookAndPublishesEnrichEventForNonExistingUserBook() {
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

        ArgumentCaptor<EnrichBookEvent> eventCaptor = ArgumentCaptor.forClass(EnrichBookEvent.class);
        verify(kafkaEnrichTemplate, times(1)).send(eq("enrich-book"), eventCaptor.capture());
        assertEquals(book.getId(), eventCaptor.getValue().bookId());
        assertEquals(user.getId(), eventCaptor.getValue().userId());
    }

    @Test
    public void testGetOrCreateBookReturnsExistingBookByIsbn() {
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        Book result = bookService.getOrCreateBook(book.getTitle(), book.getAuthor(), book.getIsbn());

        assertSame(book, result, "Existing book found by isbn should be returned");
        verify(bookRepository, times(1)).findByIsbn(book.getIsbn());
        verify(bookRepository, never()).findByTitleAndAuthor(any(), any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    public void testGetOrCreateBookReturnsExistingBookByTitleAndAuthorWhenIsbnNotFound() {
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.findByTitleAndAuthor(book.getTitle(), book.getAuthor())).thenReturn(Optional.of(book));

        Book result = bookService.getOrCreateBook(book.getTitle(), book.getAuthor(), book.getIsbn());

        assertSame(book, result, "Existing book found by title/author should be returned");
        verify(bookRepository, times(1)).findByIsbn(book.getIsbn());
        verify(bookRepository, times(1)).findByTitleAndAuthor(book.getTitle(), book.getAuthor());
        verify(bookRepository, never()).save(any());
    }

    @Test
    public void testGetOrCreateBookSkipsIsbnLookupWhenIsbnIsNull() {
        when(bookRepository.findByTitleAndAuthor(book.getTitle(), book.getAuthor())).thenReturn(Optional.of(book));

        Book result = bookService.getOrCreateBook(book.getTitle(), book.getAuthor(), null);

        assertSame(book, result);
        verify(bookRepository, never()).findByIsbn(any());
        verify(bookRepository, times(1)).findByTitleAndAuthor(book.getTitle(), book.getAuthor());
    }

    @Test
    public void testGetOrCreateBookCreatesNewBookWhenNotFoundByIsbnOrTitleAndAuthor() {
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.findByTitleAndAuthor(book.getTitle(), book.getAuthor())).thenReturn(Optional.empty());
        when(bookRepository.existsByIsbn(book.getIsbn())).thenReturn(false);
        when(bookRepository.existsByTitleAndAuthor(book.getTitle(), book.getAuthor())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.getOrCreateBook(book.getTitle(), book.getAuthor(), book.getIsbn());

        assertNotNull(result, "A new book should be created and returned");
        assertEquals(book.getTitle(), result.getTitle());
        assertEquals(book.getAuthor(), result.getAuthor());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void testGetBookByIdReturnsBookForExistingId() {
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(book.getId());

        assertSame(book, result);
        verify(bookRepository, times(1)).findById(book.getId());
    }

    @Test
    public void testGetBookByIdThrowsExceptionForNonExistingId() {
        Integer nonExistingId = 999;
        when(bookRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        ResourceNotFoundException exc = assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.getBookById(nonExistingId)
        );

        assertEquals("Book not found", exc.getMessage());
        verify(bookRepository, times(1)).findById(nonExistingId);
    }

    @Test
    public void testGetBookReferenceByIdReturnsRepositoryReference() {
        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);

        Book result = bookService.getBookReferenceById(book.getId());

        assertSame(book, result);
        verify(bookRepository, times(1)).getReferenceById(book.getId());
    }

    @Test
    public void testEmbedBookSetsEmbeddingAndSaves() {
        float[] vector = new float[]{0.1f, 0.2f, 0.3f};
        EnrichedBook enrichedBook = new EnrichedBook(book.getId(), book.getTitle(), book.getAuthor(),
                "Summary", List.of("fantasy"), List.of("loss"), List.of("dark"), List.of("dragon"));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        bookService.embedBook(enrichedBook, vector);

        assertArrayEquals(vector, book.getEmbedding(), "Embedding should be set on the book");
        verify(bookRepository, times(1)).findById(book.getId());
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    public void testEmbedBookThrowsExceptionWhenBookNotFound() {
        float[] vector = new float[]{0.1f};
        EnrichedBook enrichedBook = new EnrichedBook(999, "Title", "Author", "Summary",
                List.of(), List.of(), List.of(), List.of());
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> bookService.embedBook(enrichedBook, vector));
        verify(bookRepository, never()).save(any());
    }

    @Test
    public void testGetSimilarBooksFromLibraryDelegatesToRepository() {
        float[] queryVector = new float[]{0.5f};
        List<Integer> expected = List.of(3, 1, 2);
        when(bookRepository.retrieveBookIdsSimilarToUserQuery(user.getId(), queryVector)).thenReturn(expected);

        List<Integer> result = bookService.getSimilarBooksFromLibrary(user.getId(), queryVector);

        assertEquals(expected, result);
        verify(bookRepository, times(1)).retrieveBookIdsSimilarToUserQuery(user.getId(), queryVector);
    }

    @Test
    public void testIsBookAlreadySavedForUserReturnsTrueWhenSaved() {
        when(userBookRepository.existsByUser_IdAndBook_Isbn(user.getId(), book.getIsbn())).thenReturn(true);

        boolean result = bookService.isBookAlreadySavedForUser(user.getId(), book.getIsbn());

        assertTrue(result);
    }

    @Test
    public void testIsBookAlreadySavedForUserReturnsFalseWhenNotSaved() {
        when(userBookRepository.existsByUser_IdAndBook_Isbn(user.getId(), book.getIsbn())).thenReturn(false);

        boolean result = bookService.isBookAlreadySavedForUser(user.getId(), book.getIsbn());

        assertFalse(result);
    }

    @Test
    public void testEnrichBookUpdatesBookWithMetadataWhenProviderFindsResultAndDescription() {
        EnrichBookEvent event = new EnrichBookEvent(book.getId(), user.getId());
        ProviderSearchResult searchResult = new ProviderSearchResult("provider-1", 2020, "cover-url");
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookMetadataProvider.search(book.getIsbn(), book.getTitle(), book.getAuthor()))
                .thenReturn(Optional.of(searchResult));
        when(bookMetadataProvider.fetchDescription("provider-1")).thenReturn(Optional.of("A great story"));

        bookService.enrichBook(event);

        assertEquals("A great story", book.getDescription());
        assertEquals("2020", book.getYearPublished());
        assertEquals("cover-url", book.getThumbnailURL());
        verify(userLibraryCacheService, times(1)).invalidate(user.getId());
    }

    @Test
    public void testEnrichBookHandlesSearchResultWithoutDescription() {
        EnrichBookEvent event = new EnrichBookEvent(book.getId(), user.getId());
        ProviderSearchResult searchResult = new ProviderSearchResult("provider-1", 2020, "cover-url");
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookMetadataProvider.search(book.getIsbn(), book.getTitle(), book.getAuthor()))
                .thenReturn(Optional.of(searchResult));
        when(bookMetadataProvider.fetchDescription("provider-1")).thenReturn(Optional.empty());

        bookService.enrichBook(event);

        assertNull(book.getDescription(), "Description should stay null when provider has none");
        assertEquals("2020", book.getYearPublished());
        assertEquals("cover-url", book.getThumbnailURL());
    }

    @Test
    public void testEnrichBookLeavesMetadataNullWhenProviderFindsNoResult() {
        EnrichBookEvent event = new EnrichBookEvent(book.getId(), user.getId());
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookMetadataProvider.search(book.getIsbn(), book.getTitle(), book.getAuthor()))
                .thenReturn(Optional.empty());

        bookService.enrichBook(event);

        assertNull(book.getDescription());
        assertNull(book.getYearPublished());
        assertNull(book.getThumbnailURL());
        verify(bookMetadataProvider, never()).fetchDescription(any());
        verify(userLibraryCacheService, times(1)).invalidate(user.getId());
    }

    @Test
    public void testEnrichBookThrowsExceptionWhenBookNotFound() {
        EnrichBookEvent event = new EnrichBookEvent(999, user.getId());
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.enrichBook(event));
        verifyNoInteractions(bookMetadataProvider, userLibraryCacheService);
    }
}
