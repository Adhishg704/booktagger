package com.nemo.booktagger.client;

import lombok.Getter;
import lombok.Setter;

@Getter
public class BookMetadata {
    private final String description;
    private final String publishedDate;

    public BookMetadata(String description, String publishedDate) {
        this.description = description;
        this.publishedDate = publishedDate;
    }
}
