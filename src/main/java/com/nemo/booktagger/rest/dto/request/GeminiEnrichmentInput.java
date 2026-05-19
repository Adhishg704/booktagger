package com.nemo.booktagger.rest.dto.request;

public record GeminiEnrichmentInput(
        Integer bookId,
        String title,
        String author,
        String description
) {
}
