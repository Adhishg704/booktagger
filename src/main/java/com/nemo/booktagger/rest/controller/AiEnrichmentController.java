package com.nemo.booktagger.rest.controller;

import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.event.EmbeddingJobEvent;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.security.AuthenticatedUserService;
import com.nemo.booktagger.service.AiEnrichmentService;
import com.nemo.booktagger.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
public class AiEnrichmentController {
    private final AuthenticatedUserService authenticatedUserService;
    private final AiEnrichmentService aiEnrichmentService;
    private final JobService jobService;
    private final KafkaTemplate<String, EmbeddingJobEvent> kafkaTemplate;

    public AiEnrichmentController(AuthenticatedUserService authenticatedUserService, AiEnrichmentService aiEnrichmentService, JobService jobService,
                                  KafkaTemplate<String, EmbeddingJobEvent> kafkaTemplate) {
        this.authenticatedUserService = authenticatedUserService;
        this.aiEnrichmentService = aiEnrichmentService;
        this.jobService = jobService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/embed")
    public ResponseEntity<Integer> generateEmbeddings() {
        Integer userId = authenticatedUserService.getAuthenticatedUserId();
        Job job = jobService.createJob(0, userId);
        EmbeddingJobEvent embeddingJobEvent = new EmbeddingJobEvent(job.getId(), userId);

        kafkaTemplate.send("embed-books", embeddingJobEvent);

        return ResponseEntity.accepted().body(embeddingJobEvent.jobId());
    }

    @GetMapping("/search")
    public List<UserBookDetailedResponse> searchBooks(
            @RequestParam String query
    ) {
        Integer userId = authenticatedUserService.getAuthenticatedUserId();
        return aiEnrichmentService.getMatchingBooks(userId, query);
    }

    @GetMapping("/explain")
    public String explainBookRecommendation(
            @RequestParam Integer bookId,
            @RequestParam String query
    ) {
        Integer userId = authenticatedUserService.getAuthenticatedUserId();
        return aiEnrichmentService.getReasonForRecommendationFromAI(userId, bookId, query);
    }
}
