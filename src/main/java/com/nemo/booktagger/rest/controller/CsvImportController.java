package com.nemo.booktagger.rest.controller;

import com.nemo.booktagger.service.impl.CsvImportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/csv")
public class CsvImportController {
    private final CsvImportServiceImpl csvImportService;

    public CsvImportController(CsvImportServiceImpl csvImportService) {
        this.csvImportService = csvImportService;
    }

    @Operation(summary = "Import books from CSV")
    @PostMapping(
            value = "/{userId}/import", // More descriptive path
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Integer> importCsvBooksIntoDb(
            @PathVariable("userId") Integer userId,
            @RequestParam("csv") MultipartFile file
    ) {
        return ResponseEntity.ok(csvImportService.importCsv(file, userId));
    }
}
