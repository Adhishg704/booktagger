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
            if(isbn == null || isbn.isBlank()) {
                return getBookMetadataFromTitleAndAuthor(title, author);
            }

            Optional<BookMetadata> bookMetadata = getBookMetadataFromIsbn(isbn);
            if(bookMetadata.isEmpty())  {
                return getBookMetadataFromTitleAndAuthor(title, author);
            }

            return bookMetadata;
        }
        catch(WebClientResponseException e) {
            return Optional.empty();
        }
    }

    private Optional<BookMetadata> getBookMetadataFromTitleAndAuthor(String title, String author) {
        GoogleBooksResponse bookByTitleAndAuthor = googleBooksClient.searchByTitleAndAuthor(title, author);
        if(bookByTitleAndAuthor != null && bookByTitleAndAuthor.getItems() != null &&
                !(bookByTitleAndAuthor.getItems().isEmpty()) &&
                bookByTitleAndAuthor.getItems().getFirst().getVolumeInfo() != null) {
            BookMetadata bookMetadata = new BookMetadata(
                    bookByTitleAndAuthor.getItems().getFirst().getVolumeInfo().getDescription(),
                    bookByTitleAndAuthor.getItems().getFirst().getVolumeInfo().getPublishedDate()
            );
            return Optional.of(bookMetadata);
        }
        return Optional.empty();
    }

    private Optional<BookMetadata> getBookMetadataFromIsbn(String isbn) {
        GoogleBooksResponse bookByIsbn = googleBooksClient.searchByIsbn(isbn);
        if (bookByIsbn != null && bookByIsbn.getItems() != null &&
                !(bookByIsbn.getItems().isEmpty()) &&
                bookByIsbn.getItems().getFirst().getVolumeInfo() != null) {
            BookMetadata bookMetadata = new BookMetadata(
                    bookByIsbn.getItems().getFirst().getVolumeInfo().getDescription(),
                    bookByIsbn.getItems().getFirst().getVolumeInfo().getPublishedDate()
            );
            return Optional.of(bookMetadata);
        }
        return Optional.empty();
    }
}
