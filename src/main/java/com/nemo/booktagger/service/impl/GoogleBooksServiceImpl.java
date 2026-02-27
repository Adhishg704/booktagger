package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.client.BookMetadata;
import com.nemo.booktagger.client.GoogleBooksClient;
import com.nemo.booktagger.client.GoogleBooksResponse;
import com.nemo.booktagger.service.GoogleBooksService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Service
@Primary
public class GoogleBooksServiceImpl implements GoogleBooksService {

    private final GoogleBooksClient googleBooksClient;

    public GoogleBooksServiceImpl(GoogleBooksClient googleBooksClient) {
        this.googleBooksClient = googleBooksClient;
    }

    @Override
    public Optional<BookMetadata> getBookMetadataFromGoogleBooks(String isbn, String title, String author) {
        try {
            if (isbn != null && !isbn.isBlank()) {
                Optional<BookMetadata> byIsbn = mapToMetadata(googleBooksClient.searchByIsbn(isbn));
                if (byIsbn.isPresent()) return byIsbn;
            }

            return mapToMetadata(googleBooksClient.searchByTitleAndAuthor(title, author));
        } catch (WebClientResponseException e) {
            return Optional.empty();
        }
    }

    /**
     * Helper method to handle the deeply nested Google Books JSON structure safely.
     */
    private Optional<BookMetadata> mapToMetadata(GoogleBooksResponse response) {
        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            return Optional.empty();
        }

        var firstItem = response.getItems().getFirst();
        var volumeInfo = firstItem.getVolumeInfo();

        if (volumeInfo == null) {
            return Optional.empty();
        }
        String thumb = (volumeInfo.getImageLinks() != null)
                ? volumeInfo.getImageLinks().getThumbnail()
                : null;

        return Optional.of(new BookMetadata(
                volumeInfo.getDescription(),
                volumeInfo.getPublishedDate(),
                thumb
        ));
    }
}
