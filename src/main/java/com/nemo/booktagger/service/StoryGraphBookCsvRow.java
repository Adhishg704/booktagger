package com.nemo.booktagger.service;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryGraphBookCsvRow {

    @CsvBindByName(column = "Title")
    private String title;

    @CsvBindByName(column = "Authors")
    private String author;

    @CsvBindByName(column = "ISBN/UID")
    private String isbn;

    @CsvBindByName(column = "Read Status")
    private String status;

    @CsvBindByName(column = "Last Date Read")
    private String dateRead;

    @CsvBindByName(column = "Moods")
    private String moods;

    @CsvBindByName(column = "Pace")
    private String pace;

    @CsvBindByName(column = "Star Rating")
    private double rating;
}
