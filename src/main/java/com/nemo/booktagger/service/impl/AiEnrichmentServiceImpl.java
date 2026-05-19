package com.nemo.booktagger.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.rest.dto.request.GeminiEnrichmentInput;
import com.nemo.booktagger.rest.dto.response.EnrichedBook;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.service.AiEnrichmentService;
import com.nemo.booktagger.service.BookService;
import com.nemo.booktagger.service.UserLibraryCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiEnrichmentServiceImpl implements AiEnrichmentService {
    private final UserLibraryCacheService userLibraryCacheService;
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;
    private final BookService bookService;

    private static final List<String> GENRES = List.of(
            "fantasy", "science fiction", "romance", "mystery", "thriller",
            "historical", "literary", "horror", "non-fiction", "young adult",
            "magical realism", "contemporary"
    );

    public AiEnrichmentServiceImpl(UserLibraryCacheService userLibraryCacheService, ChatClient chatClient,
                                   EmbeddingModel embeddingModel, ObjectMapper objectMapper, BookService bookService) {
        this.userLibraryCacheService = userLibraryCacheService;
        this.chatClient = chatClient;
        this.embeddingModel = embeddingModel;
        this.objectMapper = objectMapper;
        this.bookService = bookService;
    }

    @Override
    public void createEmbeddingsUsingGemini(Integer userId) {
        List<Object[]> userLibrary = userLibraryCacheService.getUserLibrary(userId);
        List<GeminiEnrichmentInput> allBooksGeminiInput = mapToDTO(userLibrary);
        List<List<GeminiEnrichmentInput>> geminiInputBatches = batchGeminiInputsWithBatchSize(allBooksGeminiInput, 10);

        for (List<GeminiEnrichmentInput> geminiInputBatch : geminiInputBatches) {
            String response = callGemini(getPrompt(geminiInputBatch));
            List<EnrichedBook> books = parseResponse(response);
            embedBooksInBatch(books);
        }
    }

    private List<GeminiEnrichmentInput> mapToDTO(List<Object[]> userLibrary) {
        Map<Integer, GeminiEnrichmentInput> bookIdToGeminiInputMap = new HashMap<>();

        for (Object[] row : userLibrary) {
            UserBook userBook = (UserBook) row[0];
            Book book = userBook.getBook();

            bookIdToGeminiInputMap.computeIfAbsent(book.getId(),
                    k -> new GeminiEnrichmentInput(
                            book.getId(),
                            book.getTitle(),
                            book.getAuthor(),
                            book.getDescription()
                    ));
        }

        return new ArrayList<>(bookIdToGeminiInputMap.values());
    }

    private List<List<GeminiEnrichmentInput>> batchGeminiInputsWithBatchSize(List<GeminiEnrichmentInput> allBooksGeminiInput, int batchSize) {
        List<List<GeminiEnrichmentInput>> batchedBooks = new ArrayList<>();

        for (int i = 0; i < allBooksGeminiInput.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allBooksGeminiInput.size());
            batchedBooks.add(new ArrayList<>(allBooksGeminiInput.subList(i, end)));
        }

        return batchedBooks;
    }

    private String getPrompt(List<GeminiEnrichmentInput> batch) {
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

    private List<EnrichedBook> parseResponse(String response) {
        if (response == null || !response.trim().startsWith("[")) {
            log.error("Invalid Gemini response: {}", response);
            return List.of();
        }

        try {
            return objectMapper.readValue(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", response, e);
            return List.of();
        }
    }

    private void embedBooksInBatch(List<EnrichedBook> enrichedBooks) {
        for (EnrichedBook enrichedBook : enrichedBooks) {
            try {
                float[] vector = embeddingModel.embed(buildEmbeddingText(enrichedBook));
                bookService.embedBook(enrichedBook, vector);
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

    private List<String> safeList(List<String> list) {
        return list == null ? List.of() : list;
    }

    @Override
    public List<UserBookDetailedResponse> getMatchingBooks(Integer userId, String userInput) {
        List<UserBookDetailedResponse> results = new ArrayList<>();
        Map<Integer, UserBookDetailedResponse> bookIdToUserBookDetailedResponseMap = createBookIdToUserBookDetailedResponseMap(userId);
        float[] embeddedUserInput = embeddingModel.embed(userInput);
        List<Integer> similarBooksFromLibrary = bookService.getSimilarBooksFromLibrary(userId, embeddedUserInput);

        for(Integer bookId: similarBooksFromLibrary) {
            if(bookIdToUserBookDetailedResponseMap.containsKey(bookId)) {
                results.add(bookIdToUserBookDetailedResponseMap.get(bookId));
            }
        }

        return results;
    }

    private Map<Integer, UserBookDetailedResponse> createBookIdToUserBookDetailedResponseMap(Integer userId) {
        List<Object[]> userLibrary = userLibraryCacheService.getUserLibrary(userId);
        Map<Integer, UserBookDetailedResponse> bookIdToDetailedResponseMap = new HashMap<>();

        for(Object[] userBookAndBookTag: userLibrary) {
            UserBook userBook = (UserBook) userBookAndBookTag[0];
            BookTag bookTag = (BookTag) userBookAndBookTag[1];
            Book book = userBook.getBook();

            UserBookDetailedResponse response = bookIdToDetailedResponseMap.computeIfAbsent(book.getId(),
                    k -> new UserBookDetailedResponse(
                            book.getTitle(),
                            book.getAuthor(),
                            book.getYearPublished(),
                            userBook.getYearRead(),
                            book.getDescription(),
                            book.getThumbnailURL(),
                            userBook.getRating(),
                            new ArrayList<>()
                    ));

            if(bookTag == null || bookTag.getTag() == null) {
                continue;
            }

            Tag tag = bookTag.getTag();
            String tagName = tag.getTagName();

            if(!response.tags().contains(tagName)) {
                response.tags().add(tagName);
            }
        }

        return bookIdToDetailedResponseMap;
    }
}