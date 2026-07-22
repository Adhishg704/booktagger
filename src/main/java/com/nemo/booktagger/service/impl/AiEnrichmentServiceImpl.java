package com.nemo.booktagger.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemo.booktagger.entity.AiEmbeddedBook;
import com.nemo.booktagger.event.EmbeddingJobEvent;
import com.nemo.booktagger.rest.dto.request.GeminiEnrichmentInput;
import com.nemo.booktagger.rest.dto.response.ai.EnrichedBook;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.service.AiEmbeddedBookService;
import com.nemo.booktagger.service.AiEnrichmentService;
import com.nemo.booktagger.service.BookService;
import com.nemo.booktagger.service.JobService;
import com.nemo.booktagger.service.UserLibraryCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiEnrichmentServiceImpl implements AiEnrichmentService {
    private final UserLibraryCacheService userLibraryCacheService;
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;
    private final BookService bookService;
    private final AiEmbeddedBookService aiEmbeddedBookService;
    private final JobService jobService;

    private static final int BATCH_SIZE = 10;

    private static final List<String> GENRES = List.of(
            "fantasy", "science fiction", "romance", "mystery", "thriller",
            "historical", "literary", "horror", "non-fiction", "young adult",
            "magical realism", "contemporary"
    );

    public AiEnrichmentServiceImpl(UserLibraryCacheService userLibraryCacheService, ChatClient chatClient,
                                   EmbeddingModel embeddingModel, ObjectMapper objectMapper, BookService bookService,
                                   AiEmbeddedBookService aiEmbeddedBookService, JobService jobService) {
        this.userLibraryCacheService = userLibraryCacheService;
        this.chatClient = chatClient;
        this.embeddingModel = embeddingModel;
        this.objectMapper = objectMapper;
        this.bookService = bookService;
        this.aiEmbeddedBookService = aiEmbeddedBookService;
        this.jobService = jobService;
    }

    @Override
    @KafkaListener(topics = "embed-books")
    public void createEmbeddingsUsingGemini(EmbeddingJobEvent embeddingJobEvent) {

        Integer jobId = embeddingJobEvent.jobId();
        jobService.markRunning(jobId);

        Integer userId = embeddingJobEvent.userId();

        List<UserBookDetailedResponse> userLibrary =
                userLibraryCacheService.getUserLibrary(userId).books();

        List<GeminiEnrichmentInput> allBooksGeminiInput =
                mapToDTO(userLibrary);

        jobService.updateTotal(jobId, allBooksGeminiInput.size());

        List<List<GeminiEnrichmentInput>> geminiInputBatches =
                batchGeminiInputsWithBatchSize(allBooksGeminiInput, BATCH_SIZE);

        int processed = 0;
        boolean failed = false;

        for (List<GeminiEnrichmentInput> geminiInputBatch : geminiInputBatches) {
            try {
                String response = callGemini(getEmbeddingPrompt(geminiInputBatch));
                List<EnrichedBook> books = parseEmbeddingResponse(response);

                embedBooksInBatch(books);

                processed += books.size();
                jobService.updateProgress(jobId, processed);

            } catch (Exception e) {
                log.error("Embedding failed for batch: {}", processed / BATCH_SIZE, e);
                failed = true;
            }
        }

        if (failed) {
            jobService.markFailed(jobId);
        } else {
            jobService.markCompleted(jobId);
        }
    }

    private List<GeminiEnrichmentInput> mapToDTO(List<UserBookDetailedResponse> userLibrary) {
        return userLibrary.stream()
                .map(book -> new GeminiEnrichmentInput(
                        book.id(),
                        book.title(),
                        book.author(),
                        book.description()
                ))
                .toList();
    }

    private List<List<GeminiEnrichmentInput>> batchGeminiInputsWithBatchSize(List<GeminiEnrichmentInput> allBooksGeminiInput, int batchSize) {
        List<List<GeminiEnrichmentInput>> batchedBooks = new ArrayList<>();

        for (int i = 0; i < allBooksGeminiInput.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allBooksGeminiInput.size());
            batchedBooks.add(new ArrayList<>(allBooksGeminiInput.subList(i, end)));
        }

        return batchedBooks;
    }

    private String callGemini(String prompt) {
        try {
            return chatClient.prompt()
                    .system("You are a strict JSON generator. Output only valid JSON")
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Gemini call failed", e);
            return null;
        }
    }

    private List<EnrichedBook> parseEmbeddingResponse(String response) {
        return parseListResponse(
                response,
                new TypeReference<>() {
                }
        );
    }

    private void embedBooksInBatch(List<EnrichedBook> enrichedBooks) {
        for (EnrichedBook enrichedBook : enrichedBooks) {
            try {
                float[] vector = embeddingModel.embed(buildEmbeddingText(enrichedBook));
                bookService.embedBook(enrichedBook, vector);
                AiEmbeddedBook aiEmbeddedBook = new AiEmbeddedBook(
                        bookService.getBookReferenceById(enrichedBook.bookId()),
                        enrichedBook.title(),
                        enrichedBook.author(),
                        enrichedBook.summary(),
                        String.join(", ", enrichedBook.genres()),
                        String.join(", ", enrichedBook.themes()),
                        String.join(", ", enrichedBook.tones()),
                        String.join(", ", enrichedBook.keywords())
                );
                aiEmbeddedBookService.save(aiEmbeddedBook);
            } catch (Exception e) {
                log.error("Embedding failed for bookId={}", enrichedBook.bookId(), e);
            }
        }
    }

    private String buildEmbeddingText(EnrichedBook book) {
        return """
            Title: %s
            Author: %s
            Summary: %s
            Genres: %s
            Themes: %s
            Tones: %s
            Keywords: %s
            """
                .formatted(
                        book.title(),
                        book.author(),
                        book.summary(),
                        String.join(", ", safeList(book.genres())),
                        String.join(", ", safeList(book.themes())),
                        String.join(", ", safeList(book.tones())),
                        String.join(", ", safeList(book.keywords()))
                );
    }

    private String getEmbeddingPrompt(List<GeminiEnrichmentInput> batch) {
        try {
            String booksJson = objectMapper.writeValueAsString(batch);
            String genres = String.join(", ", GENRES);

            return """
                You are a strict JSON generator for book enrichment and semantic tagging.

                TASK:
                For each book, generate structured metadata to improve semantic search and recommendations.

                OUTPUT FORMAT (STRICT JSON ONLY):
                [
                  {
                    "bookId": number,
                    "title": "string",
                    "author": "string",
                    "summary": "string",
                    "genres": ["string"],
                    "themes": ["string"],
                    "tones": ["string"],
                    "keywords": ["string"]
                  }
                ]

                RULES:
                - Output MUST be valid JSON
                - DO NOT include markdown or explanations
                - ALWAYS return an array
                - Preserve bookId EXACTLY as given
                - bookId MUST be a number (not string)
                - Preserve title and author EXACTLY as given

                CONTENT RULES:
                - Summary:
                  - 2–3 concise lines
                  - Focus on core premise and emotional arc

                - Genres:
                  - MUST be chosen ONLY from:
                    %s
                  - Choose 1–3 most relevant genres

                - Themes:
                  - Abstract ideas (e.g., "identity", "loss", "freedom", "power")
                  - 3–6 items

                - Tones:
                  - Emotional feel (e.g., "dark", "hopeful", "melancholic", "tense")
                  - 2–4 items

                - Keywords:
                  - Specific searchable terms (e.g., "dragon", "space war", "forbidden love")
                  - 5–10 items

                ADDITIONAL RULES:
                - If description is missing or insufficient:
                  - Use general knowledge if the book is well-known
                  - Otherwise infer cautiously from title and author
                - DO NOT invent specific plot details if unsure
                - Prefer generic but accurate summaries over speculation
                - If unsure, keep outputs high-level and minimal
                - If genres are unclear, choose the closest match from the allowed list
                - If themes or tones are uncertain, return fewer items rather than guessing

                BOOKS:
                %s
                """.formatted(genres, booksJson);

        } catch (Exception e) {
            throw new RuntimeException("Failed to build prompt", e);
        }
    }

    private String buildExplanationPrompt(String userInput, EnrichedBook book) {
        try {
            String bookJson = objectMapper.writeValueAsString(book);

            return """
        You are a book recommendation explainer.

        USER QUERY:
        %s

        BOOK:
        %s

        TASK:
        Explain why this book matches the user query.

        RULES:
        - 2–3 sentences only
        - Focus on themes, tone, emotional match
        - Be specific, not generic
        - Do NOT repeat metadata
        - Do NOT be verbose

        OUTPUT:
        Return plain text only (no JSON, no markdown)
        """.formatted(userInput, bookJson);

        } catch (Exception e) {
            throw new RuntimeException("Failed to build explanation prompt", e);
        }
    }

    private List<String> safeList(List<String> list) {
        return list == null ? List.of() : list;
    }

    @Override
    public List<UserBookDetailedResponse> getMatchingBooks(Integer userId, String userInput) {
        List<UserBookDetailedResponse> userLibrary = userLibraryCacheService.getUserLibrary(userId).books();

        Map<Integer, UserBookDetailedResponse> booksById = userLibrary.stream()
                .collect(Collectors.toMap(
                        UserBookDetailedResponse::id,
                        Function.identity()
                ));

        float[] embeddedUserInput = embeddingModel.embed(userInput);
        List<Integer> similarBooksFromLibrary =
                bookService.getSimilarBooksFromLibrary(userId, embeddedUserInput);

        List<UserBookDetailedResponse> results = new ArrayList<>();

        for (Integer bookId : similarBooksFromLibrary) {
            UserBookDetailedResponse book = booksById.get(bookId);
            if (book != null) {
                results.add(book);
            }
        }

        return results;
    }

    @Override
    public String getReasonForRecommendationFromAI(Integer userId, Integer bookId, String userInput) {
        AiEmbeddedBook aiEmbeddedBook =
                aiEmbeddedBookService.getAiEmbeddedBook(bookId).orElse(null);

        if (aiEmbeddedBook == null) {
            return "Book not found in library";
        }

        EnrichedBook enrichedBook = createEnrichedBookFromAiEmbeddedBook(aiEmbeddedBook);
        String prompt = buildExplanationPrompt(userInput, enrichedBook);

        return callGemini(prompt);
    }

    private EnrichedBook createEnrichedBookFromAiEmbeddedBook(AiEmbeddedBook aiEmbeddedBook) {
        return new EnrichedBook(
                aiEmbeddedBook.getBook().getId(),
                aiEmbeddedBook.getTitle(),
                aiEmbeddedBook.getAuthor(),
                aiEmbeddedBook.getSummary(),
                splitString(aiEmbeddedBook.getGenres()),
                splitString(aiEmbeddedBook.getThemes()),
                splitString(aiEmbeddedBook.getTones()),
                splitString(aiEmbeddedBook.getKeywords())
        );
    }

    private List<String> splitString(String value) {
        if(value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(", "))
                .map(String::trim)
                .toList();
    }

    private <T> List<T> parseListResponse(String response, TypeReference<List<T>> typeReference) {
        if (response == null || !response.trim().startsWith("[")) {
            log.error("Invalid Gemini response: {}", response);
            return List.of();
        }
        try {
            return objectMapper.readValue(response, typeReference);
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", response, e);
            return List.of();
        }
    }
}