package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.JobRepository;
import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.enums.JobStatus;
import com.nemo.booktagger.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobServiceImplTest {

    private static final int jobId = 1;
    private static final int nonExistingJobId = 2;
    private static final String jobNotFoundExceptionMessage = "Job with id " + nonExistingJobId + " has not been created";

    private Job job;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    public void setUp() {
        job = new Job(JobStatus.PENDING, 0, 100, 5);
    }

    @Test
    public void testCreateJobSavesAndReturnsJob() {
        int total = 100;
        Integer userId = 5;
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        Job createdJob = jobService.createJob(total, userId);

        assertNotNull(createdJob, "Job should be returned");
        assertEquals(JobStatus.PENDING, createdJob.getStatus(), "New job should start as PENDING");
        assertEquals(0, createdJob.getProcessed(), "New job should start with 0 processed");

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository, times(1)).save(jobCaptor.capture());
        Job savedJob = jobCaptor.getValue();
        assertEquals(JobStatus.PENDING, savedJob.getStatus(), "Unexpected status passed to repository");
        assertEquals(total, savedJob.getTotal(), "Unexpected total passed to repository");
        assertEquals(userId, savedJob.getUserId(), "Unexpected userId passed to repository");
    }

    @Test
    public void testGetJobReturnsJobForExistingJob() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        Job returnedJob = jobService.getJob(jobId);

        assertNotNull(returnedJob, "Job should be returned");
        assertEquals(job, returnedJob, "Unexpected job returned");
        verify(jobRepository, times(1)).findById(jobId);
    }

    @Test
    public void testGetJobThrowsExceptionForNonExistingJob() {
        when(jobRepository.findById(nonExistingJobId)).thenReturn(Optional.empty());

        ResourceNotFoundException exc = assertThrows(
                ResourceNotFoundException.class,
                () -> jobService.getJob(nonExistingJobId)
        );

        assertEquals(jobNotFoundExceptionMessage, exc.getMessage(), "Unexpected exception message");
        verify(jobRepository, times(1)).findById(nonExistingJobId);
    }

    @Test
    public void testMarkRunningSetsStatusToRunningAndSaves() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);

        Job updatedJob = jobService.markRunning(jobId);

        assertEquals(JobStatus.RUNNING, updatedJob.getStatus(), "Job status should be RUNNING");
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    public void testMarkRunningThrowsExceptionForNonExistingJob() {
        when(jobRepository.findById(nonExistingJobId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.markRunning(nonExistingJobId));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    public void testMarkCompletedSetsStatusToCompletedAndSaves() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);

        Job updatedJob = jobService.markCompleted(jobId);

        assertEquals(JobStatus.COMPLETED, updatedJob.getStatus(), "Job status should be COMPLETED");
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    public void testMarkCompletedThrowsExceptionForNonExistingJob() {
        when(jobRepository.findById(nonExistingJobId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.markCompleted(nonExistingJobId));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    public void testMarkFailedSetsStatusToFailedAndSaves() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);

        Job updatedJob = jobService.markFailed(jobId);

        assertEquals(JobStatus.FAILED, updatedJob.getStatus(), "Job status should be FAILED");
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    public void testMarkFailedThrowsExceptionForNonExistingJob() {
        when(jobRepository.findById(nonExistingJobId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.markFailed(nonExistingJobId));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    public void testUpdateProgressSetsProcessedAndSaves() {
        int processed = 42;
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);

        Job updatedJob = jobService.updateProgress(jobId, processed);

        assertEquals(processed, updatedJob.getProcessed(), "Job processed count should be updated");
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    public void testUpdateProgressThrowsExceptionForNonExistingJob() {
        when(jobRepository.findById(nonExistingJobId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.updateProgress(nonExistingJobId, 10));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    public void testUpdateTotalSetsTotalAndSaves() {
        int total = 250;
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);

        Job updatedJob = jobService.updateTotal(jobId, total);

        assertEquals(total, updatedJob.getTotal(), "Job total should be updated");
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    public void testUpdateTotalThrowsExceptionForNonExistingJob() {
        when(jobRepository.findById(nonExistingJobId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.updateTotal(nonExistingJobId, 10));
        verify(jobRepository, never()).save(any(Job.class));
    }
}
