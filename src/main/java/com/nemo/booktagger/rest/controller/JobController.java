package com.nemo.booktagger.rest.controller;

import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Job> getJob(
            @PathVariable Integer jobId
    ) {
        return ResponseEntity.ok(
                jobService.getJob(jobId)
        );
    }
}
