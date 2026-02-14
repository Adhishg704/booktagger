package com.nemo.booktagger.service;

import com.nemo.booktagger.client.BookMetadata;

import java.util.Optional;

public interface GoogleBooksService {
    Optional<BookMetadata> getBookMetadataFromGoogleBooks(String isbn, String title, String author);
}
