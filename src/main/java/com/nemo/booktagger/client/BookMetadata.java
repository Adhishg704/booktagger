package com.nemo.booktagger.client;

import lombok.Getter;

@Getter
public class BookMetadata {
    private final String description;
    private final String publishedDate;
    private final String thumbnailURL;

    public BookMetadata(String description, String publishedDate, String thumbnailURL) {
        this.description = description;
        this.publishedDate = publishedDate;
        this.thumbnailURL = thumbnailURL;
    }
}
