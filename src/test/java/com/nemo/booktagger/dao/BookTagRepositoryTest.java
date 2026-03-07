package com.nemo.booktagger.dao;

import com.nemo.booktagger.base.BaseRepositoryTest;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.enums.TagType;
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
            "Speculative fiction",
            "Dark"
    );

    @BeforeEach
    public void setUp() {
        setUpBookTagRepositoryData();
    }

    @Test
    public void testFindByUserIdAndBookId() {
        Book firstBook = testBooks.getFirst();
        List<BookTag> bookTagList = bookTagRepository.findByUser_IdAndBook_Id(testUser.getId(), firstBook.getId());

        assertEquals(TAG_COUNT, bookTagList.size(), "There should be 4 tags associated with each book");
        for(BookTag bookTag: bookTagList) {
            assertEquals(testUser.getId(), bookTag.getUser().getId());
            assertEquals(firstBook.getId(), bookTag.getBook().getId());
            assertTrue(expectedTagsSet.contains(bookTag.getTag().getTagName()));
        }
    }

    @Test
    public void testFindByUserIdAndTagName() {
        BookTag bookTag = testBookTags.getFirst();
        Tag tag = bookTag.getTag();
        Book book = bookTag.getBook();

        List<BookTag> bookTagList = bookTagRepository.findByUser_IdAndTag_TagName(testUser.getId(), tag.getTagName());
        BookTag returnedBookTag = bookTagList.getFirst();
        Tag returnedTag = returnedBookTag.getTag();
        Book returnedBook = returnedBookTag.getBook();

        assertEquals(BOOK_COUNT, bookTagList.size(), "There should be 10 books associated with each tag");
        assertEquals(tag.getTagName(), returnedTag.getTagName(), "Tag names should be same");
        assertEquals(book.getTitle(), returnedBook.getTitle(), "Book names should be same");
    }

    @Test
    public void testExistsByUserIdAndBookIdAndTagId() {
        Book firstBook = testBooks.getFirst();
        Tag firstTag = testTags.getFirst();
        assertTrue(bookTagRepository.existsByUser_IdAndBook_IdAndTag_Id(testUser.getId(), firstBook.getId(),
                firstTag.getId()));
    }

    @Test
    public void testGetDistinctTagTypes() {
        List<TagType> distinctTagTypes = bookTagRepository.getDistinctTagTypes(testUser.getId());
        for(BookTag bookTag: testBookTags) {
            assertTrue(distinctTagTypes.contains(bookTag.getTag().getTagType()),
                    "Tag type not present in distinctTagTypes");
        }
    }
}