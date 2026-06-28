package com.nemo.booktagger.rest.controller;

import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.event.ImportJobEvent;
import com.nemo.booktagger.service.JobService;
import com.nemo.booktagger.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/csv")
public class CsvImportController {
    private final KafkaTemplate<String, ImportJobEvent> kafkaTemplate;
    private final JobService jobService;
    private final StorageService storageService;
    private static final Logger log =
            LoggerFactory.getLogger(CsvImportController.class);

    public CsvImportController(KafkaTemplate<String, ImportJobEvent> template, JobService jobService, StorageService storageService) {
        kafkaTemplate = template;
        this.jobService = jobService;
        this.storageService = storageService;
    }

    @Operation(summary = "Import books from CSV")
    @PostMapping(
            value = "/{userId}/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Integer> importCsvBooksIntoDb(
            @PathVariable("userId") Integer userId,
            @RequestParam("csv") MultipartFile file
    ) throws IOException {
        String fileKey = storageService.upload(file.getInputStream(), file.getOriginalFilename());

        Job job = jobService.createJob(0, userId);
        ImportJobEvent importJobEvent = new ImportJobEvent(job.getId(), fileKey);
        kafkaTemplate.send("import-books", importJobEvent);

        return ResponseEntity.accepted().body(importJobEvent.jobId());
    }
}
