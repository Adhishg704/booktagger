package com.nemo.booktagger.service;

import com.nemo.booktagger.event.ImportJobEvent;

public interface CsvImportService {
    void importCsv(ImportJobEvent jobEvent);
}
