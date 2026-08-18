package com.nemo.booktagger.integration;

import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.event.ImportJobEvent;
import com.nemo.booktagger.service.CsvImportService;
import com.nemo.booktagger.service.JobService;
import com.nemo.booktagger.service.StorageService;
import com.nemo.booktagger.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("h2")
public class CsvImportIntegrationTest {

    private static final int STORYGRAPH_ROWS = 183;

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @Autowired
    private StorageService storageService;

    @Test
    public void testBooksImportedFromCsvIntoDb() throws IOException {
        User user = userService.createUser("user123", "user1239o3@gmail.com");
        Job job = jobService.createJob(STORYGRAPH_ROWS, user.getId());

        ClassPathResource resource = new ClassPathResource("Storygraph_library.csv");
        String fileKey;
        try (InputStream inputStream = resource.getInputStream()) {
            fileKey = storageService.upload(inputStream, "Storygraph_library.csv");
        }

        csvImportService.importCsv(new ImportJobEvent(job.getId(), fileKey));

        Job updatedJob = jobService.getJob(job.getId());
        assertEquals(STORYGRAPH_ROWS, updatedJob.getProcessed(), "Unexpected number of books imported");
    }
}
