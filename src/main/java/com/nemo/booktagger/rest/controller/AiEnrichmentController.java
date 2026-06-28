package com.nemo.booktagger.rest.controller;

import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.event.EmbeddingJobEvent;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.service.AiEnrichmentService;
import com.nemo.booktagger.service.JobService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final JobService jobService;
    private final KafkaTemplate<String, EmbeddingJobEvent> kafkaTemplate;

    public AiEnrichmentController(AiEnrichmentService aiEnrichmentService, JobService jobService,
                                  KafkaTemplate<String, EmbeddingJobEvent> kafkaTemplate) {
        this.aiEnrichmentService = aiEnrichmentService;
        this.jobService = jobService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/embed/{userId}")
    public ResponseEntity<Integer> generateEmbeddings(@PathVariable Integer userId) {
        Job job = jobService.createJob(0, userId);
        EmbeddingJobEvent embeddingJobEvent = new EmbeddingJobEvent(job.getId(), userId);

        kafkaTemplate.send("embed-books", embeddingJobEvent);

        return ResponseEntity.accepted().body(embeddingJobEvent.jobId());
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
