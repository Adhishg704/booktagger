package com.nemo.booktagger.rest.dto.response;

import com.nemo.booktagger.enums.ReadingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserBookResponse {

    private String title;
    private String author;
    private String yearPublished;
    private String yearRead;
    private ReadingStatus status;
    private Double rating;
}
