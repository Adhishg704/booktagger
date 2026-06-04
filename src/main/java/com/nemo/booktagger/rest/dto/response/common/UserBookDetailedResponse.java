package com.nemo.booktagger.rest.dto.response.common;

import java.util.ArrayList;
import java.util.List;

public record UserBookDetailedResponse(
        Integer id,
        String title,
        String author,
        String yearPublished,
        String yearRead,
        String description,
        String thumbnailURL,
        Double rating,
        List<String> tags
) {

    public UserBookDetailedResponse {
        if(tags == null) {
            tags = new ArrayList<>();
        }
    }

}
