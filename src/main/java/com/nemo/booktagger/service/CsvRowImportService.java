package com.nemo.booktagger.service;

public interface CsvRowImportService {

    void processRow(Integer userId, StoryGraphBookCsvRow row);
}
