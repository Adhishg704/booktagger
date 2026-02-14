package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.client.BookMetadata;
import com.nemo.booktagger.client.GoogleBooksClient;
import com.nemo.booktagger.client.GoogleBooksResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleBooksServiceImplTest {

    @Mock
    private GoogleBooksClient googleBooksClient;

    @InjectMocks
    private GoogleBooksServiceImpl googleBooksService;

    private final String isbn = "1234567891123";
    private final String yearPublished = "2025";
    private final String title = "Title";
    private final String author = "Author";
    private final String description= "Description";

    @Test
    public void testGoogleBooksServiceReturnsBookMetadataForValidIsbn() {
        GoogleBooksResponse response = createGoogleBooksResponse();
        when(googleBooksClient.searchByIsbn(eq(isbn))).thenReturn(response);

        Optional<BookMetadata> bookMetadataFromGoogleBooks = googleBooksService
                .getBookMetadataFromGoogleBooks(isbn, title, author);

        verifyValidBookMetadata(bookMetadataFromGoogleBooks, response);
        verify(googleBooksClient, never()).searchByTitleAndAuthor(anyString(), anyString());
        verify(googleBooksClient, times(1)).searchByIsbn(eq(isbn));
    }

    @Test
    public void testGoogleBooksServiceReturnsBookMetadataForInvalidIsbnValidTitleAndAuthor() {
        GoogleBooksResponse response = createGoogleBooksResponse();
        when(googleBooksClient.searchByTitleAndAuthor(title, author)).thenReturn(response);

        Optional<BookMetadata> bookMetadataFromGoogleBooks = googleBooksService
                .getBookMetadataFromGoogleBooks("", title, author);

        verifyValidBookMetadata(bookMetadataFromGoogleBooks, response);
        verify(googleBooksClient, never()).searchByIsbn(anyString());
        verify(googleBooksClient, times(1)).searchByTitleAndAuthor(eq(title), eq(author));
    }

    @Test
    public void testGoogleBooksServiceReturnsNoBookMetadataForInvalidIsbnInvalidTitleAndAuthor() {
        GoogleBooksResponse response = createEmptyGoogleBooksResponse();
        when(googleBooksClient.searchByTitleAndAuthor(title, author)).thenReturn(response);

        Optional<BookMetadata> bookMetadataFromGoogleBooks = googleBooksService
                .getBookMetadataFromGoogleBooks("", title, author);

        assertTrue(bookMetadataFromGoogleBooks.isEmpty(), "Empty data should be returned for invalid input");
        verify(googleBooksClient, never()).searchByIsbn(anyString());
        verify(googleBooksClient, times(1)).searchByTitleAndAuthor(eq(title), eq(author));
    }

    @Test
    public void testGoogleBooksServiceReturnsNoBookMetadataForValidIsbnReturnsEmptyInvalidTitleAndAuthor() {
        GoogleBooksResponse response = createEmptyGoogleBooksResponse();
        when(googleBooksClient.searchByIsbn(eq(isbn))).thenReturn(response);
        when(googleBooksClient.searchByTitleAndAuthor(title, author)).thenReturn(response);

        Optional<BookMetadata> bookMetadataFromGoogleBooks = googleBooksService
                .getBookMetadataFromGoogleBooks(isbn, title, author);

        assertTrue(bookMetadataFromGoogleBooks.isEmpty(), "Empty data should be returned for invalid input");
        verify(googleBooksClient, times(1)).searchByIsbn(eq(isbn));
        verify(googleBooksClient, times(1)).searchByTitleAndAuthor(eq(title), eq(author));
    }

    @Test
    public void testGoogleBooksServiceReturnsNoBookMetadataForValidIsbnReturnsEmptyValidTitleAndAuthor() {
        GoogleBooksResponse emptyResponse = createEmptyGoogleBooksResponse();
        GoogleBooksResponse response = createGoogleBooksResponse();
        when(googleBooksClient.searchByIsbn(eq(isbn))).thenReturn(emptyResponse);
        when(googleBooksClient.searchByTitleAndAuthor(title, author)).thenReturn(response);

        Optional<BookMetadata> bookMetadataFromGoogleBooks = googleBooksService
                .getBookMetadataFromGoogleBooks(isbn, title, author);

        verifyValidBookMetadata(bookMetadataFromGoogleBooks, response);
        verify(googleBooksClient, times(1)).searchByIsbn(eq(isbn));
        verify(googleBooksClient, times(1)).searchByTitleAndAuthor(eq(title), eq(author));
    }

    private GoogleBooksResponse createEmptyGoogleBooksResponse() {
        GoogleBooksResponse response = new GoogleBooksResponse();
        return response;
    }

    private GoogleBooksResponse createGoogleBooksResponse() {
        GoogleBooksResponse response = new GoogleBooksResponse();

        GoogleBooksResponse.VolumeInfo volumeInfo = new GoogleBooksResponse.VolumeInfo();
        volumeInfo.setDescription(description);
        volumeInfo.setPublishedDate(yearPublished);

        GoogleBooksResponse.Item item = new GoogleBooksResponse.Item();
        item.setVolumeInfo(volumeInfo);

        response.setItems(List.of(item));

        return response;
    }

    private void verifyValidBookMetadata(Optional<BookMetadata> bookMetadata, GoogleBooksResponse response) {
        assertTrue(bookMetadata.isPresent(), "Metadata should be returned as ISBN is valid");
        assertEquals(response.getItems().getFirst().getVolumeInfo().getDescription(),
                bookMetadata.get().getDescription(), "Description should match");
        assertEquals(response.getItems().getFirst().getVolumeInfo().getPublishedDate(),
                bookMetadata.get().getPublishedDate(), "Published date should match");
    }
}