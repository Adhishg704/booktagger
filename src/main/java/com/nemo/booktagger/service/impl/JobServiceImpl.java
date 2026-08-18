package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.JobRepository;
import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.enums.JobStatus;
import com.nemo.booktagger.exception.ResourceNotFoundException;
import com.nemo.booktagger.service.JobService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    @Transactional
    public Job createJob(int total, Integer userId) {
        return jobRepository.save(
                new Job(
                        JobStatus.PENDING,
                        0,
                        total,
                        userId
                )
        );
    }

    @Override
    public Job getJob(int jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job with id " + jobId + " has not been created"
                        ));
    }

    @Override
    @Transactional
    public Job markRunning(int jobId) {
        Job job = getJob(jobId);
        job.setStatus(JobStatus.RUNNING);
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job markCompleted(int jobId) {
        Job job = getJob(jobId);
        job.setStatus(JobStatus.COMPLETED);
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job markFailed(int jobId) {
        Job job = getJob(jobId);
        job.setStatus(JobStatus.FAILED);
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job updateProgress(int jobId, int processed) {
        Job job = getJob(jobId);
        job.setProcessed(processed);
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job updateTotal(int jobId, int total) {
        Job job = getJob(jobId);
        job.setTotal(total);
        return jobRepository.save(job);
    }
}
