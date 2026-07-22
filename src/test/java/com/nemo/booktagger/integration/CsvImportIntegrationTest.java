package com.nemo.booktagger.integration;

import com.nemo.booktagger.entity.Job;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.event.ImportJobEvent;
import com.nemo.booktagger.service.CsvImportService;
import com.nemo.booktagger.service.JobService;
import com.nemo.booktagger.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

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

    @SuppressWarnings("unused")
    @Test
    public void testBooksImportedFromCsvIntoDb() throws IOException {
        User user = userService.createUser("user123", "user1239o3@gmail.com");
        Job job = jobService.createJob(STORYGRAPH_ROWS, user.getId());

        ClassPathResource resource = new ClassPathResource("Storygraph_library.csv");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Storygraph_library.csv",
                "text/csv",
                resource.getInputStream()
        );

        csvImportService.importCsv(new ImportJobEvent(job.getId(), user.getPassword()));

        assertEquals(STORYGRAPH_ROWS, job.getProcessed(), "Unexpected number of books imported");
    }
}
