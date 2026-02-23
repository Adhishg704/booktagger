package com.nemo.booktagger.rest.dto.request;

import com.nemo.booktagger.enums.ReadingStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBookFilterRequest {

    private String yearRead;
    private String yearPublished;
    private ReadingStatus status;
}
