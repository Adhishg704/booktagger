package com.nemo.booktagger.rest.dto.response.common;

import java.util.List;

public record UserLibraryCache(
        List<UserBookDetailedResponse> books
) {
}
