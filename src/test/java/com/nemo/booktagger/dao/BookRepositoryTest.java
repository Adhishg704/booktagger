package com.nemo.booktagger.dao;

import com.nemo.booktagger.base.BaseRepositoryTest;
import com.nemo.booktagger.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class BookRepositoryTest extends BaseRepositoryTest {

    @BeforeEach
    public void setUp() {
        setUpBookRepositoryData();
    }

    @Test
    public void findTitleById() {
        Book firstBook = testBooks.getFirst();
        Optional<String> expectedBookTitle = bookRepository.findTitleById(firstBook.getId());
        assertTrue(expectedBookTitle.isPresent());
        assertEquals(firstBook.getTitle(), expectedBookTitle.get(), "Wrong title returned by repository");
    }

    @Test
    public void findAuthorById() {
        Book firstBook = testBooks.getFirst();
        Optional<String> expectedBookAuthor = bookRepository.findAuthorById(firstBook.getId());
        assertTrue(expectedBookAuthor.isPresent());
        assertEquals(firstBook.getAuthor(), expectedBookAuthor.get(), "Wrong author returned by repository");
    }

    @Test
    public void findDescriptionById() {
        Book firstBook = testBooks.getFirst();
        Optional<String> expectedBookDescription = bookRepository.findDescriptionById(firstBook.getId());
        assertTrue(expectedBookDescription.isPresent());
        assertEquals(firstBook.getDescription(), expectedBookDescription.get(), "Wrong description returned by repository");
    }

    @Test
    public void findIsbnById() {
        Book firstBook = testBooks.getFirst();
        Optional<Long> expectedBookIsbn = bookRepository.findIsbnById(firstBook.getId());
        assertTrue(expectedBookIsbn.isPresent());
        assertEquals(firstBook.getIsbn(), expectedBookIsbn.get(), "Wrong ISBN returned by repository");
    }

    @Test
    public void findYearPublishedById() {
        Book firstBook = testBooks.getFirst();
        Optional<String> expectedBookYearPublished = bookRepository.findYearPublishedById(firstBook.getId());
        assertTrue(expectedBookYearPublished.isPresent());
        assertEquals(firstBook.getYearPublished(), expectedBookYearPublished.get(), "Wrong title returned by repository");
    }
}