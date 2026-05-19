package com.nemo.booktagger.rest.dto.response;

import java.util.List;

public record EnrichedBook(
        Integer bookId,
        String title,
        String author,
        String summary,
        List<String> genres,
        List<String> themes,
        List<String> tones,
        List<String> keywords
) {
}
