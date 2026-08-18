package com.nemo.booktagger.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemo.booktagger.entity.AiEmbeddedBook;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.event.EmbeddingJobEvent;
import com.nemo.booktagger.exception.ResourceNotFoundException;
import com.nemo.booktagger.rest.dto.response.ai.EnrichedBook;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.rest.dto.response.common.UserLibraryCache;
import com.nemo.booktagger.service.AiEmbeddedBookService;
import com.nemo.booktagger.service.BookService;
import com.nemo.booktagger.service.JobService;
import com.nemo.booktagger.service.UserLibraryCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiEnrichmentServiceImplTest {

    private static final Integer userId = 1;
    private static final Integer jobId = 100;

    @Mock
    private UserLibraryCacheService userLibraryCacheService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private BookService bookService;

    @Mock
    private AiEmbeddedBookService aiEmbeddedBookService;

    @Mock
    private JobService jobService;

    private ObjectMapper objectMapper;

    private AiEnrichmentServiceImpl aiEnrichmentService;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        aiEnrichmentService = new AiEnrichmentServiceImpl(
                userLibraryCacheService, chatClient, embeddingModel, objectMapper,
                bookService, aiEmbeddedBookService, jobService
        );
    }

    private void stubGeminiResponse(String json) {
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content()).thenReturn(json);
    }

    private UserBookDetailedResponse book(Integer id, String title, String author, String description) {
        return new UserBookDetailedResponse(id, title, author, "2025", "2025", description, "thumb", 4.0, List.of());
    }

    private String enrichedBookJson(int bookId, String title, String author) {
        return """
                [
                  {
                    "bookId": %d,
                    "title": "%s",
                    "author": "%s",
                    "summary": "A great book.",
                    "genres": ["fantasy"],
                    "themes": ["identity", "loss"],
                    "tones": ["dark"],
                    "keywords": ["dragon", "magic"]
                  }
                ]
                """.formatted(bookId, title, author);
    }

    private String enrichedBooksJson(List<Integer> bookIds) {
        String entries = bookIds.stream()
                .map(id -> """
                        {
                          "bookId": %d,
                          "title": "Book%d",
                          "author": "Author%d",
                          "summary": "A great book.",
                          "genres": ["fantasy"],
                          "themes": ["identity"],
                          "tones": ["dark"],
                          "keywords": ["dragon"]
                        }
                        """.formatted(id, id, id))
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        return "[" + entries + "]";
    }

    // ---- createEmbeddingsUsingGemini ----

    @Test
    public void testCreateEmbeddingsUsingGeminiHappyPathEmbedsAndCompletesJob() {
        EmbeddingJobEvent event = new EmbeddingJobEvent(jobId, userId);
        UserBookDetailedResponse libraryBook = book(10, "Dune", "Frank Herbert", "Desc");
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(libraryBook)));
        stubGeminiResponse(enrichedBookJson(10, "Dune", "Frank Herbert"));

        Book bookEntity = mock(Book.class);
        when(bookService.getBookReferenceById(10)).thenReturn(bookEntity);
        float[] vector = new float[]{0.1f, 0.2f};
        when(embeddingModel.embed(anyString())).thenReturn(vector);

        aiEnrichmentService.createEmbeddingsUsingGemini(event);

        verify(jobService, times(1)).markRunning(jobId);
        verify(jobService, times(1)).updateTotal(jobId, 1);
        verify(jobService, times(1)).updateProgress(jobId, 1);
        verify(jobService, times(1)).markCompleted(jobId);
        verify(jobService, never()).markFailed(anyInt());

        verify(bookService, times(1)).embedBook(argThat(eb -> eb.bookId() == 10), eq(vector));

        ArgumentCaptor<AiEmbeddedBook> captor = ArgumentCaptor.forClass(AiEmbeddedBook.class);
        verify(aiEmbeddedBookService, times(1)).save(captor.capture());
        AiEmbeddedBook saved = captor.getValue();
        assertEquals("Dune", saved.getTitle());
        assertEquals("Frank Herbert", saved.getAuthor());
        assertEquals("fantasy", saved.getGenres());
        assertEquals("identity, loss", saved.getThemes());
        assertEquals("dark", saved.getTones());
        assertEquals("dragon, magic", saved.getKeywords());
        assertSame(bookEntity, saved.getBook());
    }

    @Test
    public void testCreateEmbeddingsUsingGeminiEmptyLibraryMarksCompletedWithoutCallingGemini() {
        EmbeddingJobEvent event = new EmbeddingJobEvent(jobId, userId);
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of()));

        aiEnrichmentService.createEmbeddingsUsingGemini(event);

        verify(jobService, times(1)).markRunning(jobId);
        verify(jobService, times(1)).updateTotal(jobId, 0);
        verify(jobService, never()).updateProgress(anyInt(), anyInt());
        verify(jobService, times(1)).markCompleted(jobId);
        verify(jobService, never()).markFailed(anyInt());
        verifyNoInteractions(chatClient, embeddingModel, aiEmbeddedBookService);
        verify(bookService, never()).embedBook(any(), any());
    }

    @Test
    public void testCreateEmbeddingsUsingGeminiInvalidJsonResponseSkipsBooksButStillCompletes() {
        EmbeddingJobEvent event = new EmbeddingJobEvent(jobId, userId);
        UserBookDetailedResponse libraryBook = book(10, "Dune", "Frank Herbert", "Desc");
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(libraryBook)));
        stubGeminiResponse("not valid json");

        aiEnrichmentService.createEmbeddingsUsingGemini(event);

        verify(jobService, times(1)).updateProgress(jobId, 0);
        verify(jobService, times(1)).markCompleted(jobId);
        verify(jobService, never()).markFailed(anyInt());
        verifyNoInteractions(embeddingModel, aiEmbeddedBookService);
    }

    @Test
    public void testCreateEmbeddingsUsingGeminiNullResponseFromChatClientSkipsBooksButStillCompletes() {
        EmbeddingJobEvent event = new EmbeddingJobEvent(jobId, userId);
        UserBookDetailedResponse libraryBook = book(10, "Dune", "Frank Herbert", "Desc");
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(libraryBook)));
        stubGeminiResponse(null);

        aiEnrichmentService.createEmbeddingsUsingGemini(event);

        verify(jobService, times(1)).updateProgress(jobId, 0);
        verify(jobService, times(1)).markCompleted(jobId);
        verify(jobService, never()).markFailed(anyInt());
    }

    @Test
    public void testCreateEmbeddingsUsingGeminiChatClientThrowingIsTreatedAsEmptyResponse() {
        EmbeddingJobEvent event = new EmbeddingJobEvent(jobId, userId);
        UserBookDetailedResponse libraryBook = book(10, "Dune", "Frank Herbert", "Desc");
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(libraryBook)));
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenThrow(new RuntimeException("Gemini unavailable"));

        aiEnrichmentService.createEmbeddingsUsingGemini(event);

        verify(jobService, times(1)).updateProgress(jobId, 0);
        verify(jobService, times(1)).markCompleted(jobId);
        verify(jobService, never()).markFailed(anyInt());
    }

    @Test
    public void testCreateEmbeddingsUsingGeminiMarksJobFailedWhenBatchProcessingThrows() {
        EmbeddingJobEvent event = new EmbeddingJobEvent(jobId, userId);
        UserBookDetailedResponse libraryBook = book(10, "Dune", "Frank Herbert", "Desc");
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(libraryBook)));

        ObjectMapper failingMapper = mock(ObjectMapper.class);
        try {
            when(failingMapper.writeValueAsString(any())).thenThrow(new RuntimeException("serialization boom"));
        } catch (Exception e) {
            fail(e);
        }
        AiEnrichmentServiceImpl serviceWithFailingMapper = new AiEnrichmentServiceImpl(
                userLibraryCacheService, chatClient, embeddingModel, failingMapper,
                bookService, aiEmbeddedBookService, jobService
        );

        serviceWithFailingMapper.createEmbeddingsUsingGemini(event);

        verify(jobService, never()).updateProgress(anyInt(), anyInt());
        verify(jobService, never()).markCompleted(jobId);
        verify(jobService, times(1)).markFailed(jobId);
        verifyNoInteractions(chatClient);
    }

    @Test
    public void testCreateEmbeddingsUsingGeminiProcessesMultipleBatches() {
        EmbeddingJobEvent event = new EmbeddingJobEvent(jobId, userId);
        List<UserBookDetailedResponse> books = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(i -> book(i, "Book" + i, "Author" + i, "Desc" + i))
                .toList();
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(books));

        // BATCH_SIZE is 10, so 12 books split into two batches: ids 1-10, then ids 11-12.
        // Gemini is called once per batch, so stub one response per call, each matching that batch's real size.
        List<Integer> firstBatchIds = java.util.stream.IntStream.rangeClosed(1, 10).boxed().toList();
        List<Integer> secondBatchIds = java.util.stream.IntStream.rangeClosed(11, 12).boxed().toList();
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(enrichedBooksJson(firstBatchIds), enrichedBooksJson(secondBatchIds));

        Book bookEntity = mock(Book.class);
        when(bookService.getBookReferenceById(anyInt())).thenReturn(bookEntity);
        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f});

        aiEnrichmentService.createEmbeddingsUsingGemini(event);

        verify(jobService, times(1)).updateTotal(jobId, 12);
        verify(jobService, times(1)).updateProgress(jobId, 10);
        verify(jobService, times(1)).updateProgress(jobId, 12);
        verify(jobService, times(1)).markCompleted(jobId);
        verify(jobService, never()).markFailed(anyInt());

        verify(aiEmbeddedBookService, times(12)).save(any(AiEmbeddedBook.class));

        ArgumentCaptor<EnrichedBook> embedCaptor = ArgumentCaptor.forClass(EnrichedBook.class);
        verify(bookService, times(12)).embedBook(embedCaptor.capture(), any());
        List<Integer> embeddedBookIds = embedCaptor.getAllValues().stream()
                .map(EnrichedBook::bookId)
                .toList();
        assertEquals(
                java.util.stream.IntStream.rangeClosed(1, 12).boxed().toList(),
                embeddedBookIds.stream().sorted().toList(),
                "Every book across both batches should have been embedded exactly once"
        );
    }

    @Test
    public void testCreateEmbeddingsUsingGeminiContinuesWhenSingleBookEmbeddingFails() {
        EmbeddingJobEvent event = new EmbeddingJobEvent(jobId, userId);
        UserBookDetailedResponse libraryBook = book(10, "Dune", "Frank Herbert", "Desc");
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(libraryBook)));
        stubGeminiResponse(enrichedBookJson(10, "Dune", "Frank Herbert"));

        when(embeddingModel.embed(anyString())).thenThrow(new RuntimeException("embedding service down"));

        aiEnrichmentService.createEmbeddingsUsingGemini(event);

        // the batch is still counted as processed even though the individual book embed failed
        verify(jobService, times(1)).updateProgress(jobId, 1);
        verify(jobService, times(1)).markCompleted(jobId);
        verify(jobService, never()).markFailed(anyInt());
        verify(bookService, never()).embedBook(any(), any());
        verify(aiEmbeddedBookService, never()).save(any());
    }

    // ---- getMatchingBooks ----

    @Test
    public void testGetMatchingBooksReturnsBooksInSimilarityOrder() {
        UserBookDetailedResponse book1 = book(1, "Book1", "Author1", "Desc1");
        UserBookDetailedResponse book2 = book(2, "Book2", "Author2", "Desc2");
        UserBookDetailedResponse book3 = book(3, "Book3", "Author3", "Desc3");
        when(userLibraryCacheService.getUserLibrary(userId))
                .thenReturn(new UserLibraryCache(List.of(book1, book2, book3)));
        float[] vector = new float[]{0.5f};
        when(embeddingModel.embed("find me a dark fantasy")).thenReturn(vector);
        when(bookService.getSimilarBooksFromLibrary(userId, vector)).thenReturn(List.of(3, 1));

        List<UserBookDetailedResponse> result = aiEnrichmentService.getMatchingBooks(userId, "find me a dark fantasy");

        assertEquals(List.of(book3, book1), result, "Books should be returned in similarity order");
    }

    @Test
    public void testGetMatchingBooksSkipsIdsNotPresentInLibrary() {
        UserBookDetailedResponse book1 = book(1, "Book1", "Author1", "Desc1");
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(book1)));
        float[] vector = new float[]{0.5f};
        when(embeddingModel.embed(anyString())).thenReturn(vector);
        when(bookService.getSimilarBooksFromLibrary(userId, vector)).thenReturn(List.of(1, 99));

        List<UserBookDetailedResponse> result = aiEnrichmentService.getMatchingBooks(userId, "query");

        assertEquals(List.of(book1), result, "Unknown book ids should be silently skipped");
    }

    @Test
    public void testGetMatchingBooksReturnsEmptyListWhenNoSimilarBooksFound() {
        UserBookDetailedResponse book1 = book(1, "Book1", "Author1", "Desc1");
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(book1)));
        float[] vector = new float[]{0.5f};
        when(embeddingModel.embed(anyString())).thenReturn(vector);
        when(bookService.getSimilarBooksFromLibrary(userId, vector)).thenReturn(List.of());

        List<UserBookDetailedResponse> result = aiEnrichmentService.getMatchingBooks(userId, "query");

        assertTrue(result.isEmpty(), "Result should be empty when no similar books are found");
    }

    // ---- getReasonForRecommendationFromAI ----

    @Test
    public void testGetReasonForRecommendationThrowsExceptionWhenBookNotEmbedded() {
        when(aiEmbeddedBookService.getAiEmbeddedBook(10)).thenReturn(Optional.empty());

        ResourceNotFoundException exc = assertThrows(
                ResourceNotFoundException.class,
                () -> aiEnrichmentService.getReasonForRecommendationFromAI(userId, 10, "why this book?")
        );

        assertEquals("Book not found in library", exc.getMessage());
        verifyNoInteractions(chatClient);
    }

    @Test
    public void testGetReasonForRecommendationReturnsAiGeneratedExplanation() {
        Book bookEntity = mock(Book.class);
        when(bookEntity.getId()).thenReturn(10);
        AiEmbeddedBook aiEmbeddedBook = new AiEmbeddedBook(
                bookEntity, "Dune", "Frank Herbert", "Summary",
                "fantasy, sci-fi", "identity, loss", "dark, hopeful", "dragon, magic"
        );
        when(aiEmbeddedBookService.getAiEmbeddedBook(10)).thenReturn(Optional.of(aiEmbeddedBook));
        stubGeminiResponse("This book matches because of its dark tone.");

        String reason = aiEnrichmentService.getReasonForRecommendationFromAI(userId, 10, "something dark");

        assertEquals("This book matches because of its dark tone.", reason);
        verify(aiEmbeddedBookService, times(1)).getAiEmbeddedBook(10);
    }

    @Test
    public void testGetReasonForRecommendationHandlesNullAndBlankMetadataGracefully() {
        Book bookEntity = mock(Book.class);
        when(bookEntity.getId()).thenReturn(10);
        AiEmbeddedBook aiEmbeddedBook = new AiEmbeddedBook(
                bookEntity, "Dune", "Frank Herbert", "Summary",
                null, "", "   ", "keyword1, keyword2"
        );
        when(aiEmbeddedBookService.getAiEmbeddedBook(10)).thenReturn(Optional.of(aiEmbeddedBook));
        stubGeminiResponse("Explanation text.");

        String reason = aiEnrichmentService.getReasonForRecommendationFromAI(userId, 10, "something");

        assertEquals("Explanation text.", reason, "Null/blank genre-like fields should not cause failures");
    }
}
