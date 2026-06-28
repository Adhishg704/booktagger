package com.nemo.booktagger.service;

import com.nemo.booktagger.entity.Job;

public interface JobService {

    Job createJob(int total, Integer userId);

    Job getJob(int jobId);

    Job markRunning(int jobId);

    Job markCompleted(int jobId);

    Job markFailed(int jobId);

    Job updateProgress(int jobId, int processed);

    Job updateTotal(int jobId, int total);
}
