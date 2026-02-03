package com.nemo.booktagger.dao;

import com.nemo.booktagger.base.BaseRepositoryTest;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.enums.TagType;
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
public class TagRepositoryTest extends BaseRepositoryTest {

    @BeforeEach
    public void setUp() {
        setUpTagRepositoryData();
    }

    @Test
    public void testFindTagNameById() {
        Tag firstTag = testTags.getFirst();
        Optional<String> returnedTagName = tagRepository.findTagNameById(firstTag.getId());
        assertTrue(returnedTagName.isPresent(), "Tag should exist");
        assertEquals(firstTag.getTagName(), returnedTagName.get(), "Wrong tag name returned by repository");
    }

    @Test
    public void testFindTagTypeById() {
        Tag firstTag = testTags.getFirst();
        Optional<TagType> returnedTagType = tagRepository.findTagTypeById(firstTag.getId());
        assertTrue(returnedTagType.isPresent(), "Tag should exist");
        assertEquals(firstTag.getTagType(), returnedTagType.get(), "Wrong tag type returned by repository");
    }

    @Test
    public void testFindByIdAndUserId() {
        Tag firstTag = testTags.getFirst();
        Optional<Tag> returnedTag = tagRepository.findByIdAndUser_Id(firstTag.getId(), testUser.getId());

        assertTrue(returnedTag.isPresent(), "Tag should exist");
        Tag tag = returnedTag.get();
        assertEquals(firstTag.getTagName(), tag.getTagName(), "Wrong tag name returned by repository");
        assertEquals(firstTag.getTagType(), tag.getTagType(), "Wrong tag type returned by repository");
    }

    @Test
    public void testExistsByUserIdAndTagNameAndTagType() {
        Tag firstTag = testTags.getFirst();
        assertTrue(tagRepository.existsByUser_IdAndTagNameAndTagType(testUser.getId(),
                firstTag.getTagName(), firstTag.getTagType()), "Tag should exist");
    }
}
