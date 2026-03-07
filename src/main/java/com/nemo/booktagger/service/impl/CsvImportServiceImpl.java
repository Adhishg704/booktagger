package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.service.CsvImportService;
import com.nemo.booktagger.service.CsvRowImportService;
import com.nemo.booktagger.service.StoryGraphBookCsvRow;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

@Service
public class CsvImportServiceImpl implements CsvImportService {
    private final CsvRowImportService csvRowImportService;

    public CsvImportServiceImpl(CsvRowImportService csvRowImportService) {
        this.csvRowImportService = csvRowImportService;
    }

    @Override
    @CacheEvict(value = "userLibrary", key = "#userId")
    public Integer importCsv(MultipartFile file, Integer userId) {
        CsvToBean<StoryGraphBookCsvRow> bookCsvParser = createBookCsvParser(file);
        Iterator<StoryGraphBookCsvRow> it = bookCsvParser.iterator();
        int rows = 0;

        while(it.hasNext()) {
            StoryGraphBookCsvRow row = it.next();
            csvRowImportService.processRow(userId, row);
            rows ++;
        }

        return rows;
    }

    private CsvToBean<StoryGraphBookCsvRow> createBookCsvParser(MultipartFile file) {
        HeaderColumnNameMappingStrategy<StoryGraphBookCsvRow> strategy =
                new HeaderColumnNameMappingStrategy<>();
        strategy.setType(StoryGraphBookCsvRow.class);

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream())
            );

            return new CsvToBeanBuilder<StoryGraphBookCsvRow>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
        }
        catch(IOException e) {
            throw new RuntimeException("Failed to read csv: " + e);
        }
    }
}
