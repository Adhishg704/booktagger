package com.nemo.booktagger.client.openlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibrarySearchResponse(
        @JsonProperty("docs") List<OLDoc> docs
) {
}

