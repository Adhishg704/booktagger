package com.nemo.booktagger.service;

import com.nemo.booktagger.event.EmbeddingJobEvent;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;

import java.util.List;

public interface AiEnrichmentService {
    void createEmbeddingsUsingGemini(EmbeddingJobEvent embeddingJobEvent);

    List<UserBookDetailedResponse> getMatchingBooks(Integer userId, String userInput);

    String getReasonForRecommendationFromAI(Integer userId, Integer bookId, String userInput);
}
