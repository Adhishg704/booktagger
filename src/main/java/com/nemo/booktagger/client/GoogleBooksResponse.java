package com.nemo.booktagger.client;

import lombok.Data;

import java.util.List;

@Data
public class GoogleBooksResponse {

    private List<Item> items;

    @Data
    public static class Item {
        private VolumeInfo volumeInfo;
    }

    @Data
    public static class VolumeInfo {
        private String description;
        private String publishedDate;
        private ImageLinks imageLinks;
    }

    @Data
    public static class ImageLinks {
        private String thumbnail;
    }
}
