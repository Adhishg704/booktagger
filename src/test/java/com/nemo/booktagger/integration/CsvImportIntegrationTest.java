package com.nemo.booktagger.integration;

import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.service.BookService;
import com.nemo.booktagger.service.CsvImportService;
import com.nemo.booktagger.service.TagService;
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
    private BookService bookService;

    @Autowired
    private TagService tagService;

    @Test
    public void testBooksImportedFromCsvIntoDb() throws IOException {
        User user = userService.createUser("user123", "user1239o3@gmail.com");

        ClassPathResource resource = new ClassPathResource("Storygraph_library.csv");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Storygraph_library.csv",
                "text/csv",
                resource.getInputStream()
        );

        int imported = csvImportService.importCsv(file, user.getId());

        assertEquals(STORYGRAPH_ROWS, imported, "Unexpected number of books imported");
    }
}
