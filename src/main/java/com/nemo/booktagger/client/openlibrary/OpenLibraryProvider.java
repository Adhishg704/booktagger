package com.nemo.booktagger.client.openlibrary;

import com.nemo.booktagger.client.BookMetadataProvider;
import com.nemo.booktagger.client.ProviderSearchResult;
import com.nemo.booktagger.client.openlibrary.dto.OpenLibrarySearchResponse;
import com.nemo.booktagger.client.openlibrary.dto.OpenLibraryWorkResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Primary
@Service
public class OpenLibraryProvider implements BookMetadataProvider {
    private final WebClient openLibraryClient;

    public OpenLibraryProvider(WebClient openLibraryWebClient) {
        this.openLibraryClient = openLibraryWebClient;
    }

    @Override
    public Optional<ProviderSearchResult> search(String isbn, String title, String author) {
        if(isbn != null && !isbn.isBlank()) {
            Optional<ProviderSearchResult> providerSearchResult = executeSearch("isbn:" + isbn);
            if(providerSearchResult.isPresent()) {
                return providerSearchResult;
            }
        }
        String titleAuthorQuery = String.format("title:\"%s\" author:\"%s\"", title, author);
        return executeSearch(titleAuthorQuery);
    }

    @Override
    public Optional<String> fetchDescription(String workId) {
        OpenLibraryWorkResponse response = withRetry(openLibraryClient.get().uri(
                uriBuilder -> uriBuilder.
                        path(workId + ".json").
                        build()
        ).retrieve().bodyToMono(OpenLibraryWorkResponse.class));

        if(response == null || response.description() == null) {
            return Optional.empty();
        }
        return Optional.of(parseDescription(response.description()));
    }

    private String parseDescription(Object description) {
        if(description instanceof String s) {
            return s;
        }
        else if(description instanceof Map<?,?> map) {
            return (String) map.get("value");
        }
        return "";
    }

    private Optional<ProviderSearchResult> executeSearch(String query) {
        OpenLibrarySearchResponse response = withRetry(openLibraryClient.get().uri(
                uriBuilder -> uriBuilder.
                        path("/search.json").
                        queryParam("q", query).
                        queryParam("limit", 1).
                        build()

        ).retrieve().bodyToMono(OpenLibrarySearchResponse.class));

        if(response == null || response.docs() == null || response.docs().isEmpty()) {
            return Optional.empty();
        }

        var doc = response.docs().getFirst();

        return Optional.of(new ProviderSearchResult(
                doc.workKey(),
                doc.firstYearPublished(),
                constructCoverURL(doc.coverI())
        ));
    }

    private String constructCoverURL(Long coverI) {
        if(coverI == null || coverI <= 0) {
            return null;
        }
        return "https://covers.openlibrary.org/b/id/" + coverI + "-L.jpg";
    }

    private <T> T withRetry(Mono<T> mono) {
        return mono.retryWhen(
                Retry.backoff(3, Duration.ofSeconds(2))
        ).block();
    }
}
