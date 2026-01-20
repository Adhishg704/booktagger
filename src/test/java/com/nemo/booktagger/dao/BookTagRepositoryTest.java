package com.nemo.booktagger.dao;

import com.nemo.booktagger.base.BaseRepositoryTest;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class BookTagRepositoryTest extends BaseRepositoryTest {
    private final Set<String> expectedTagsSet = Set.of(
            "Fantasy",
            "Sci-fi",
            "Speculative fiction"
    );

    @BeforeEach
    public void setUp() {
        setUpBookTagRepositoryData();
    }

    @Test
    public void testFindByUserIdAndBookId() {
        Book firstBook = testBooks.getFirst();
        List<BookTag> bookTagList = bookTagRepository.findByUser_IdAndBook_Id(testUser.getId(), firstBook.getId());

        assertEquals(3, bookTagList.size(), "There should be 3 tags associated with each book");
        for(BookTag bookTag: bookTagList) {
            assertEquals(testUser.getId(), bookTag.getUser().getId());
            assertEquals(firstBook.getId(), bookTag.getBook().getId());
            assertTrue(expectedTagsSet.contains(bookTag.getTag().getTagName()));
        }
    }
}