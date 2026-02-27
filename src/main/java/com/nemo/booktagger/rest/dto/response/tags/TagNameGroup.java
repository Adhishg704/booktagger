package com.nemo.booktagger.rest.dto.response.tags;

import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;

import java.util.List;

public record TagNameGroup(
        String tagName,
        List<UserBookDetailedResponse> books
) {
}
