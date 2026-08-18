package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.event.ImportJobEvent;
import com.nemo.booktagger.service.CsvRowImportService;
import com.nemo.booktagger.service.JobService;
import com.nemo.booktagger.service.StorageService;
import com.nemo.booktagger.service.StoryGraphBookCsvRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CsvImportServiceImplTest {

    private static final String CSV_HEADER = "Title,Authors,ISBN/UID,Read Status,Last Date Read,Moods,Pace,Star Rating\n";
    private static final Integer jobId = 1;
    private static final Integer userId = 5;
    private static final String fileKey = "some-file-key.csv";

    private Job job;

    @Mock
    private CsvRowImportService csvRowImportService;

    @Mock
    private StorageService storageService;

    @Mock
    private JobService jobService;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private CsvImportServiceImpl csvImportService;

    @BeforeEach
    public void setUp() {
        job = mock(Job.class);
        when(job.getId()).thenReturn(jobId);
    }

    private InputStream csvWithRows(int rowCount) {
        StringBuilder sb = new StringBuilder(CSV_HEADER);
        for (int i = 0; i < rowCount; i++) {
            sb.append("Book").append(i).append(",Author").append(i).append(",ISBN").append(i)
                    .append(",read,2023-05-0").append(i + 1).append(",\"dark, emotional\",fast,4.5\n");
        }
        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testImportCsvProcessesAllRowsAndMarksJobCompleted() {
        ImportJobEvent event = new ImportJobEvent(jobId, fileKey);
        when(jobService.getJob(jobId)).thenReturn(job);
        when(job.getUserId()).thenReturn(userId);
        when(storageService.download(fileKey)).thenReturn(csvWithRows(2));
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("userLibrary")).thenReturn(cache);

        csvImportService.importCsv(event);

        verify(jobService, times(1)).markRunning(jobId);
        verify(jobService, times(1)).updateTotal(jobId, 2);
        verify(jobService, times(1)).updateProgress(jobId, 1);
        verify(jobService, times(1)).updateProgress(jobId, 2);
        verify(jobService, times(1)).markCompleted(jobId);
        verify(jobService, never()).markFailed(anyInt());
        verify(csvRowImportService, times(2)).processRow(eq(userId), any(StoryGraphBookCsvRow.class));
        verify(cache, times(1)).evict(userId);
        verify(storageService, times(1)).delete(fileKey);
    }

    @Test
    public void testImportCsvMarksJobFailedWhenDownloadThrows() {
        ImportJobEvent event = new ImportJobEvent(jobId, fileKey);
        when(jobService.getJob(jobId)).thenReturn(job);
        when(storageService.download(fileKey)).thenThrow(new RuntimeException("file missing"));

        csvImportService.importCsv(event);

        verify(jobService, times(1)).markRunning(jobId);
        verify(jobService, times(1)).markFailed(jobId);
        verify(jobService, never()).markCompleted(anyInt());
        verify(csvRowImportService, never()).processRow(any(), any());
        verify(storageService, times(1)).delete(fileKey);
    }

    @Test
    public void testImportCsvMarksJobFailedWhenRowProcessingThrows() {
        ImportJobEvent event = new ImportJobEvent(jobId, fileKey);
        when(jobService.getJob(jobId)).thenReturn(job);
        when(job.getUserId()).thenReturn(userId);
        when(storageService.download(fileKey)).thenReturn(csvWithRows(1));
        doThrow(new RuntimeException("row processing failed"))
                .when(csvRowImportService).processRow(eq(userId), any(StoryGraphBookCsvRow.class));

        csvImportService.importCsv(event);

        verify(jobService, times(1)).markFailed(jobId);
        verify(jobService, never()).markCompleted(anyInt());
        verify(storageService, times(1)).delete(fileKey);
    }

    @Test
    public void testImportCsvSuppressesExceptionFromFileDeletion() {
        ImportJobEvent event = new ImportJobEvent(jobId, fileKey);
        when(jobService.getJob(jobId)).thenReturn(job);
        when(job.getUserId()).thenReturn(userId);
        when(storageService.download(fileKey)).thenReturn(csvWithRows(0));
        when(cacheManager.getCache("userLibrary")).thenReturn(null);
        doThrow(new RuntimeException("delete failed")).when(storageService).delete(fileKey);

        assertDoesNotThrow(() -> csvImportService.importCsv(event));
        verify(jobService, times(1)).markCompleted(jobId);
    }

    @Test
    public void testImportCsvHandlesMissingCacheGracefully() {
        ImportJobEvent event = new ImportJobEvent(jobId, fileKey);
        when(jobService.getJob(jobId)).thenReturn(job);
        when(job.getUserId()).thenReturn(userId);
        when(storageService.download(fileKey)).thenReturn(csvWithRows(0));
        when(cacheManager.getCache("userLibrary")).thenReturn(null);

        assertDoesNotThrow(() -> csvImportService.importCsv(event));
        verify(jobService, times(1)).updateTotal(jobId, 0);
        verify(jobService, times(1)).markCompleted(jobId);
        verify(csvRowImportService, never()).processRow(any(), any());
    }
}
