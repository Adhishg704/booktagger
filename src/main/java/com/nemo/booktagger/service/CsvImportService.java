package com.nemo.booktagger.service;

import org.springframework.web.multipart.MultipartFile;

public interface CsvImportService {
    Integer importCsv(MultipartFile file, Integer userId);
}
