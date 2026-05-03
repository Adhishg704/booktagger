package com.nemo.booktagger.client;

public record ProviderSearchResult(
        String providerId,
        Integer yearPublished,
        String coverURL
) {
}
