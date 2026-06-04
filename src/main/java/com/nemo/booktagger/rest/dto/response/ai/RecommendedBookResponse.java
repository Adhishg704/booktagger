package com.nemo.booktagger.rest.dto.response.ai;

import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;

import java.util.List;

public record RecommendedBookResponse(
        UserBookDetailedResponse book,
        String aiSummary,
        List<String> matchedThemes,
        List<String> matchedGenres,
        List<String> matchedTones,
        List<String> matchedKeywords
) {
}
