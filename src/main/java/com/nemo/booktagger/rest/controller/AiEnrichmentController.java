package com.nemo.booktagger.rest.controller;

import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.service.AiEnrichmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
public class AiEnrichmentController {
    private final AiEnrichmentService aiEnrichmentService;

    public AiEnrichmentController(AiEnrichmentService aiEnrichmentService) {
        this.aiEnrichmentService = aiEnrichmentService;
    }

    @PostMapping("/embed/{userId}")
    public void generateEmbeddings(@PathVariable Integer userId) {
        aiEnrichmentService.createEmbeddingsUsingGemini(userId);
    }

    @GetMapping("/search/{userId}")
    public List<UserBookDetailedResponse> searchBooks(
            @PathVariable Integer userId,
            @RequestParam String query
    ) {
        return aiEnrichmentService.getMatchingBooks(userId, query);
    }

    @GetMapping("/explain/{userId}")
    public String explainBookRecommendation(
            @PathVariable Integer userId,
            @RequestParam Integer bookId,
            @RequestParam String query
    ) {
        return aiEnrichmentService.getReasonForRecommendationFromAI(userId, bookId, query);
    }
}
