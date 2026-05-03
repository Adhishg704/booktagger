package com.nemo.booktagger.client;

import java.util.Optional;

public interface BookMetadataProvider {
    Optional<ProviderSearchResult> search(String isbn, String title, String author);

    Optional<String> fetchDescription(String workId);
}
