package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.service.StorageService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {
    private static final String UPLOAD_DIR = "uploads";

    @Override
    public String upload(InputStream inputStream, String fileName) {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String fileKey = UUID.randomUUID() + "-" + fileName;

            Path path = Paths.get(
                    UPLOAD_DIR,
                    fileKey
            );

            Files.copy(
                    inputStream,
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return fileKey;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream download(String fileKey) {
        try {
            Path path = Paths.get(
                    UPLOAD_DIR,
                    fileKey
            );

            return Files.newInputStream(path);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String fileKey) {
        try {
            Files.deleteIfExists(
                    Paths.get(
                            UPLOAD_DIR,
                            fileKey
                    )
            );
        }
        catch (IOException e) {
            throw new RuntimeException(e)   ;
        }
    }
}
