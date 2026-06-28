package com.nemo.booktagger.service;

import java.io.InputStream;

public interface StorageService {
    String upload(
            InputStream inputStream,
            String fileName
    );

    InputStream download(String fileKey);

    void delete(String fileKey);
}
