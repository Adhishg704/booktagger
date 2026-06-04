package com.nemo.booktagger.rest.dto.request;

public record RerankedBookResult(
        Integer bookId,
        String reason
) {
}
