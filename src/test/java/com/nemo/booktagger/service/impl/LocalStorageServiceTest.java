package com.nemo.booktagger.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LocalStorageServiceTest {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    private final LocalStorageService storageService = new LocalStorageService();
    private final List<String> createdFileKeys = new ArrayList<>();

    @AfterEach
    public void cleanUp() throws IOException {
        for (String fileKey : createdFileKeys) {
            Files.deleteIfExists(UPLOAD_DIR.resolve(fileKey));
        }
    }

    @Test
    public void testUploadStoresFileAndReturnsUniqueKeyContainingOriginalFileName() {
        String content = "hello world";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String fileKey = storageService.upload(inputStream, "test.csv");
        createdFileKeys.add(fileKey);

        assertTrue(fileKey.endsWith("-test.csv"), "File key should preserve the original file name");
        assertTrue(Files.exists(UPLOAD_DIR.resolve(fileKey)), "Uploaded file should exist on disk");
    }

    @Test
    public void testUploadGeneratesDifferentKeysForSameFileName() {
        InputStream first = new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8));
        InputStream second = new ByteArrayInputStream("b".getBytes(StandardCharsets.UTF_8));

        String firstKey = storageService.upload(first, "same.csv");
        String secondKey = storageService.upload(second, "same.csv");
        createdFileKeys.add(firstKey);
        createdFileKeys.add(secondKey);

        assertNotEquals(firstKey, secondKey, "Each upload should get a unique key even for the same file name");
    }

    @Test
    public void testDownloadReturnsPreviouslyUploadedContent() throws IOException {
        String content = "downloaded content";
        String fileKey = storageService.upload(
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), "download.csv");
        createdFileKeys.add(fileKey);

        try (InputStream downloaded = storageService.download(fileKey)) {
            String actual = new String(downloaded.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(content, actual, "Downloaded content should match uploaded content");
        }
    }

    @Test
    public void testDownloadThrowsRuntimeExceptionForMissingFile() {
        assertThrows(RuntimeException.class, () -> storageService.download("does-not-exist.csv"));
    }

    @Test
    public void testDeleteRemovesUploadedFile() {
        String fileKey = storageService.upload(
                new ByteArrayInputStream("to be deleted".getBytes(StandardCharsets.UTF_8)), "delete.csv");

        storageService.delete(fileKey);

        assertFalse(Files.exists(UPLOAD_DIR.resolve(fileKey)), "File should no longer exist after delete");
    }

    @Test
    public void testDeleteDoesNotThrowForMissingFile() {
        assertDoesNotThrow(() -> storageService.delete("never-uploaded.csv"));
    }
}
