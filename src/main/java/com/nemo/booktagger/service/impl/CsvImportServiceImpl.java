package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.event.ImportJobEvent;
import com.nemo.booktagger.service.CsvImportService;
import com.nemo.booktagger.service.CsvRowImportService;
import com.nemo.booktagger.service.JobService;
import com.nemo.booktagger.service.StorageService;
import com.nemo.booktagger.service.StoryGraphBookCsvRow;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class CsvImportServiceImpl implements CsvImportService {
    private final CsvRowImportService csvRowImportService;
    private final StorageService storageService;
    private final JobService jobService;
    private final CacheManager cacheManager;

    public CsvImportServiceImpl(CsvRowImportService csvRowImportService, StorageService storageService,
                                JobService jobService, CacheManager cacheManager) {
        this.csvRowImportService = csvRowImportService;
        this.storageService = storageService;
        this.jobService = jobService;
        this.cacheManager = cacheManager;
    }

    @Override
    @KafkaListener(topics = "import-books")
    public void importCsv(ImportJobEvent jobEvent) {
        Job job = jobService.getJob(jobEvent.jobId());

        try {
            processImport(job, jobEvent.fileKey());
        } catch (Exception e) {
            jobService.markFailed(
                    job.getId()
            );
        } finally {
            deleteFile(jobEvent.fileKey());
        }
    }

    private void processImport(
            Job job,
            String fileKey
    ) {
        jobService.markRunning(job.getId());

        List<StoryGraphBookCsvRow> rows =
                loadRows(fileKey);

        jobService.updateTotal(
                job.getId(),
                rows.size()
        );

        importRows(job, rows);

        jobService.markCompleted(job.getId());

        evictUserLibraryCache(
                job.getUserId()
        );
    }

    private List<StoryGraphBookCsvRow> loadRows(
            String fileKey
    ) {
        return createBookCsvParser(
                storageService.download(fileKey)
        ).parse();
    }

    private void importRows(
            Job job,
            List<StoryGraphBookCsvRow> rows
    ) {
        int processed = 0;

        for (StoryGraphBookCsvRow row : rows) {
            csvRowImportService.processRow(
                    job.getUserId(),
                    row
            );

            processed++;

            jobService.updateProgress(
                    job.getId(),
                    processed
            );
        }
    }

    private void evictUserLibraryCache(
            Integer userId
    ) {
        Cache cache = cacheManager.getCache("userLibrary");

        if (cache != null) {
            cache.evict(userId);
        }
    }

    private void deleteFile(
            String fileKey
    ) {
        try {
            storageService.delete(fileKey);
        } catch (Exception ignored) {
        }
    }

    private CsvToBean<StoryGraphBookCsvRow> createBookCsvParser(InputStream file) {
        HeaderColumnNameMappingStrategy<StoryGraphBookCsvRow> strategy =
                new HeaderColumnNameMappingStrategy<>();
        strategy.setType(StoryGraphBookCsvRow.class);

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file)
            );

            return new CsvToBeanBuilder<StoryGraphBookCsvRow>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
        }
        catch(Exception e) {
            throw new RuntimeException("Failed to read csv: " + e);
        }
    }
}
