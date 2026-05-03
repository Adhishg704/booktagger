package com.nemo.booktagger.client.openlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibraryWorkResponse(
    Object description
) {
}
