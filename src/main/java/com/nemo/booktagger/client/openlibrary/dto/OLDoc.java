package com.nemo.booktagger.client.openlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OLDoc(
        @JsonProperty("key") String workKey,
        @JsonProperty("first_publish_year") Integer firstYearPublished,
        @JsonProperty("cover_i") Long coverI
) {
}
