package com.nemo.booktagger.rest.controller;

import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.event.ImportJobEvent;
import com.nemo.booktagger.security.AuthenticatedUserService;
import com.nemo.booktagger.service.JobService;
import com.nemo.booktagger.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/csv")
public class CsvImportController {
    private final AuthenticatedUserService authenticatedUserService;
    private final KafkaTemplate<String, ImportJobEvent> kafkaTemplate;
    private final JobService jobService;
    private final StorageService storageService;

    public CsvImportController(AuthenticatedUserService authenticatedUserService, KafkaTemplate<String, ImportJobEvent> kafkaTemplate, JobService jobService, StorageService storageService) {
        this.authenticatedUserService = authenticatedUserService;
        this.kafkaTemplate = kafkaTemplate;
        this.jobService = jobService;
        this.storageService = storageService;
    }

    @Operation(summary = "Import books from CSV")
    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Integer> importCsvBooksIntoDb(
            @RequestParam("csv") MultipartFile file
    ) throws IOException {
        String fileKey = storageService.upload(file.getInputStream(), file.getOriginalFilename());

        Integer authenticatedUserId = authenticatedUserService.getAuthenticatedUserId();
        Job job = jobService.createJob(0, authenticatedUserId);
        ImportJobEvent importJobEvent = new ImportJobEvent(job.getId(), fileKey);
        kafkaTemplate.send("import-books", importJobEvent);

        return ResponseEntity.accepted().body(importJobEvent.jobId());
    }
}
