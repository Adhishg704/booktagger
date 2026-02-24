package com.nemo.booktagger.rest.dto.request;

import com.nemo.booktagger.enums.ReadingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserBookFilterRequest {

    private String yearRead;
    private String yearPublished;
    private ReadingStatus status;
}
