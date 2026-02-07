package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.BookTagRepository;
import com.nemo.booktagger.dao.TagRepository;
import com.nemo.booktagger.dao.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.factory.BookFactory;
import com.nemo.booktagger.factory.BookTagFactory;
import com.nemo.booktagger.factory.TagFactory;
import com.nemo.booktagger.factory.UserFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceImplTest {
    private final Integer existingId = 1;
    private final Integer nonExistingId = 2;
    private final String tagName = "Fantasy";
    private final String nonExistingTagName = "Sci-fi";
    private final String tagNotFoundExceptionMessage = "Tag with tag id " + nonExistingId + " not found";
    private User user;
    private Tag tag;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookTagRepository bookTagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    public void setUp() {
        user = UserFactory.createUser("user1", "user1@gmail.com");
        tag = TagFactory.createTag(user, TagType.CUSTOM, tagName);
    }

    private List<BookTag> createBookTagList() {
        Book book = BookFactory.createBook();
        BookTag bookTag = BookTagFactory.createBookTag(user, book, tag);
        List<BookTag> bookTags = new ArrayList<>();
        for(int i = 0; i < 2; i ++) {
            bookTags.add(bookTag);
        }
        return bookTags;
    }

    @Test
    public void testGetTagByIdReturnsTagForExistingTag() {
        when(tagRepository.findById(existingId)).thenReturn(Optional.of(tag));

        Tag returnedTag = tagService.getTagById(existingId);

        assertNotNull(returnedTag, "Tag should exist");
        assertEquals(tag.getTagName(), returnedTag.getTagName(), "Unexpected tag name");
        assertEquals(tag.getTagType(), returnedTag.getTagType(), "Unexpected tag type");
        verify(tagRepository, times(1)).findById(existingId);
    }

    @Test
    public void testGetTagByIdThrowsExceptionForNonExistingTag() {
        when(tagRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> tagService.getTagById(nonExistingId)
        );

        assertEquals(tagNotFoundExceptionMessage, ex.getMessage(), "Unexpected exception message");
        verify(tagRepository, times(1)).findById(nonExistingId);
    }

    @Test
    public void testGetTagNameByIdReturnsTagNameForExistingTag() {
        when(tagRepository.findTagNameById(existingId)).thenReturn(Optional.of(tag.getTagName()));

        String returnedTagName = tagService.getTagNameById(existingId);

        assertEquals(tag.getTagName(), returnedTagName, "Unexpected tag name");
        verify(tagRepository, times(1)).findTagNameById(existingId);
    }

    @Test
    public void testGetTagNameByIdThrowsExceptionForNonExistingTag() {
        when(tagRepository.findTagNameById(nonExistingId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> tagService.getTagNameById(nonExistingId)
        );

        assertEquals(tagNotFoundExceptionMessage, ex.getMessage(), "Unexpected exception message");
        verify(tagRepository, times(1)).findTagNameById(nonExistingId);
    }
    
    @Test
    public void testGetTagTypeByIdReturnsTagTypeForExistingTag() {
        when(tagRepository.findTagTypeById(existingId)).thenReturn(Optional.of(tag.getTagType()));

        TagType returnedTagType = tagService.getTagTypeById(existingId);

        assertEquals(tag.getTagType(), returnedTagType, "Unexpected tag type");
        verify(tagRepository, times(1)).findTagTypeById(existingId);
    }

    @Test
    public void testGetTagTypeByIdThrowsExceptionForNonExistingTag() {
        when(tagRepository.findTagTypeById(nonExistingId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> tagService.getTagTypeById(nonExistingId)
        );

        assertEquals(tagNotFoundExceptionMessage, ex.getMessage(), "Unexpected exception message");
        verify(tagRepository, times(1)).findTagTypeById(nonExistingId);
    }

    @Test
    public void testGetBookTagsByTagNameReturnsEmptyListForNonExistingUserAndExistingTagName() {
        when(bookTagRepository.findByUser_IdAndTag_TagName(nonExistingId, tagName)).thenReturn(List.of());

        List<BookTag> bookTagsByTagName = tagService.getBookTagsByTagName(nonExistingId, tagName);

        assertTrue(bookTagsByTagName.isEmpty(), "Should return empty list for non existing user");
        verify(bookTagRepository, times(1)).findByUser_IdAndTag_TagName(nonExistingId, tagName);
    }

    @Test
    public void testGetBookTagsByTagNameReturnsEmptyListForExistingUserAndNonExistingTagName() {
        when(bookTagRepository.findByUser_IdAndTag_TagName(existingId, nonExistingTagName)).thenReturn(List.of());

        List<BookTag> bookTagsByTagName = tagService.getBookTagsByTagName(existingId, nonExistingTagName);

        assertTrue(bookTagsByTagName.isEmpty(), "Should return empty list for non existing tag name");
        verify(bookTagRepository, times(1)).findByUser_IdAndTag_TagName(existingId, nonExistingTagName);
    }

    @Test
    public void testGetBookTagsByTagNameReturnsListForExistingUserAndExistingTagName() {
        List<BookTag> expectedBookTags = createBookTagList();

        when(bookTagRepository.findByUser_IdAndTag_TagName(existingId, tagName))
                .thenReturn(expectedBookTags);

        List<BookTag> bookTagsByTagName =
                tagService.getBookTagsByTagName(existingId, tagName);

        assertFalse(bookTagsByTagName.isEmpty(), "Should return book tags for existing user and tag name");
        assertEquals(expectedBookTags.size(), bookTagsByTagName.size());
        verify(bookTagRepository, times(1))
                .findByUser_IdAndTag_TagName(existingId, tagName);
    }

    @Test
    public void testGetBookTagsByTagNameReturnsEmptyListForNonExistingUserAndNonExistingTagName() {
        when(bookTagRepository.findByUser_IdAndTag_TagName(nonExistingId, nonExistingTagName))
                .thenReturn(List.of());

        List<BookTag> bookTagsByTagName =
                tagService.getBookTagsByTagName(nonExistingId, nonExistingTagName);

        assertTrue(bookTagsByTagName.isEmpty(),
                "Should return empty list for non existing user and non existing tag name");
        verify(bookTagRepository, times(1))
                .findByUser_IdAndTag_TagName(nonExistingId, nonExistingTagName);
    }

    @Test
    public void testGetCustomTagsCreatedByUserReturnsEmptyListForNonExistingUser() {
        when(tagRepository.findByUser_IdAndTagType(nonExistingId, TagType.CUSTOM)).thenReturn(List.of());

        List<Tag> customTagsCreatedByUser = tagService.getCustomTagsCreatedByUser(nonExistingId);

        assertTrue(customTagsCreatedByUser.isEmpty(), "Should return empty list for non existing user");
        verify(tagRepository, times(1)).findByUser_IdAndTagType(nonExistingId, TagType.CUSTOM);
    }

    @Test
    public void testGetCustomTagsCreatedByUserReturnsListForExistingUser() {
        List<Tag> customTagList = TagFactory.createTagList(user, TagType.CUSTOM, "Sci-fi", "Fantasy", "Time-travel");
        when(tagRepository.findByUser_IdAndTagType(existingId, TagType.CUSTOM)).thenReturn(customTagList);

        List<Tag> customTagsCreatedByUser = tagService.getCustomTagsCreatedByUser(existingId);

        assertFalse(customTagsCreatedByUser.isEmpty(), "Should return list for existing user");
        assertEquals(customTagList.size(), customTagsCreatedByUser.size(), "Unexpected number of custom tags");
        assertSame(customTagList, customTagsCreatedByUser, "Service should return repository result directly");
        verify(tagRepository, times(1)).findByUser_IdAndTagType(existingId, TagType.CUSTOM);
    }

    @Test
    public void testGetMoodTagsForUserReturnsEmptyListForNonExistingUser() {
        when(tagRepository.findByUser_IdAndTagType(nonExistingId, TagType.MOOD)).thenReturn(List.of());

        List<Tag> moodTagsForUser = tagService.getMoodTagsForUser(nonExistingId);

        assertTrue(moodTagsForUser.isEmpty(), "Should return empty list for non existing user");
        verify(tagRepository, times(1)).findByUser_IdAndTagType(nonExistingId, TagType.MOOD);
    }

    @Test
    public void testGetMoodTagsForUserReturnsListForExistingUser() {
        List<Tag> moodTagList = TagFactory.createTagList(user, TagType.MOOD, "Adventurous", "Dark", "Tense");
        when(tagRepository.findByUser_IdAndTagType(existingId, TagType.MOOD)).thenReturn(moodTagList);

        List<Tag> moodTagsForUser = tagService.getMoodTagsForUser(existingId);

        assertFalse(moodTagsForUser.isEmpty(), "Should return list for existing user");
        assertEquals(moodTagList.size(), moodTagsForUser.size(), "Unexpected number of mood tags");
        assertSame(moodTagList, moodTagsForUser, "Service should return repository result directly");
        verify(tagRepository, times(1)).findByUser_IdAndTagType(existingId, TagType.MOOD);
    }

    @Test
    public void testGetPaceTagsForUserReturnsEmptyListForNonExistingUser() {
        when(tagRepository.findByUser_IdAndTagType(nonExistingId, TagType.PACE)).thenReturn(List.of());

        List<Tag> paceTagsForUser = tagService.getPaceTagsForUser(nonExistingId);

        assertTrue(paceTagsForUser.isEmpty(), "Should return empty list for non existing user");
        verify(tagRepository, times(1)).findByUser_IdAndTagType(nonExistingId, TagType.PACE);
    }

    @Test
    public void testGetPaceTagsForUserReturnsListForExistingUser() {
        List<Tag> paceTagList = TagFactory.createTagList(user, TagType.PACE, "Slow", "Medium", "Fast");
        when(tagRepository.findByUser_IdAndTagType(existingId, TagType.PACE)).thenReturn(paceTagList);

        List<Tag> paceTagsForUser = tagService.getPaceTagsForUser(existingId);

        assertFalse(paceTagsForUser.isEmpty(), "Should return list for existing user");
        assertEquals(paceTagList.size(), paceTagsForUser.size(), "Unexpected number of pace tags");
        assertSame(paceTagList, paceTagsForUser, "Service should return repository result directly");
        verify(tagRepository, times(1)).findByUser_IdAndTagType(existingId, TagType.PACE);
    }

    @Test
    public void testCreateTagThrowsExceptionIfTagAlreadyExists() {
        when(tagRepository.existsByUser_IdAndTagNameAndTagType(user.getId(), tag.getTagName(), tag.getTagType()))
                .thenReturn(true);

        RuntimeException exc = assertThrows(
                RuntimeException.class,
                () -> tagService.createTag(user.getId(), tag.getTagName(), tag.getTagType())
        );

        String tagExistsExceptionMessage = "Tag already exists";
        assertEquals(tagExistsExceptionMessage, exc.getMessage(), "Unexpected exception message");
        verify(tagRepository, times(1)).existsByUser_IdAndTagNameAndTagType(
                user.getId(), tag.getTagName(), tag.getTagType()
        );
        verify(tagRepository, never()).save(any());
        verify(userRepository, never()).getReferenceById(any());
    }

    @Test
    public void testCreateTagCreatesAndReturnsTagWhenTagDoesNotExist() {
        when(tagRepository.existsByUser_IdAndTagNameAndTagType(
                user.getId(), tagName, tag.getTagType()))
                .thenReturn(false);
        when(userRepository.getReferenceById(user.getId()))
                .thenReturn(user);
        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Tag createdTag = tagService.createTag(user.getId(), tagName, tag.getTagType());

        assertNotNull(createdTag);
        assertEquals(tagName, createdTag.getTagName());
        assertEquals(tag.getTagType(), createdTag.getTagType());
        assertEquals(user, createdTag.getUser());
        verify(tagRepository).existsByUser_IdAndTagNameAndTagType(
                user.getId(), tagName, tag.getTagType());
        verify(userRepository).getReferenceById(user.getId());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    public void testRenameCustomTagThrowsExceptionForNonExistingTag() {
        String oldTagName = tag.getTagName();
        when(tagRepository.findByIdAndUser_Id(nonExistingId, user.getId())).thenReturn(Optional.empty());

        RuntimeException exc = assertThrows(
                RuntimeException.class,
                () -> tagService.renameCustomTag(user.getId(), nonExistingId, "NewTagName")
        );

        assertEquals(tagNotFoundExceptionMessage, exc.getMessage(), "Unexpected exception message");
        assertEquals(oldTagName, tag.getTagName(), "Expected old tag name as tag is not renamed");
        verify(tagRepository, times(1)).findByIdAndUser_Id(nonExistingId, user.getId());
        verify(tagRepository, never()).existsByUser_IdAndTagNameAndTagType(anyInt(), anyString(), any(TagType.class));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    public void testRenameCustomTagThrowsExceptionForNonCustomTag() {
        String oldTagName = tag.getTagName();
        tag.setTagType(TagType.MOOD);
        when(tagRepository.findByIdAndUser_Id(tag.getId(), user.getId())).thenReturn(Optional.of(tag));

        RuntimeException exc = assertThrows(
                RuntimeException.class,
                () -> tagService.renameCustomTag(user.getId(), tag.getId(), "NewTagName")
        );

        String expectedExceptionMessage = "Can only rename custom tags";
        assertEquals(expectedExceptionMessage, exc.getMessage(), "Unexpected exception message");
        assertEquals(oldTagName, tag.getTagName(), "Expected old tag name as tag is not renamed");
        verify(tagRepository, times(1)).findByIdAndUser_Id(tag.getId(), user.getId());
        verify(tagRepository, never()).existsByUser_IdAndTagNameAndTagType(anyInt(), anyString(), any(TagType.class));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    public void testRenameCustomTagThrowsExceptionForNewNameMatchingExistingTagName() {
        String oldTagName = tag.getTagName();
        String newTagName = "NewTagName";
        when(tagRepository.findByIdAndUser_Id(tag.getId(), user.getId())).thenReturn(Optional.of(tag));
        when(tagRepository.existsByUser_IdAndTagNameAndTagType(user.getId(), newTagName, tag.getTagType())).thenReturn(true);

        RuntimeException exc = assertThrows(
                RuntimeException.class,
                () -> tagService.renameCustomTag(user.getId(), tag.getId(), newTagName)
        );

        String expectedExceptionMessage = "Tag with this name already exists";
        assertEquals(expectedExceptionMessage, exc.getMessage(), "Unexpected exception message");
        assertEquals(oldTagName, tag.getTagName(), "Expected old tag name as tag is not renamed");
        verify(tagRepository, times(1)).findByIdAndUser_Id(tag.getId(), user.getId());
        verify(tagRepository, times(1)).existsByUser_IdAndTagNameAndTagType(
                eq(user.getId()), eq(newTagName), eq(TagType.CUSTOM));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    public void testRenameCustomTagRenamesTag() {
        String oldTagName = tag.getTagName();
        String newTagName = "NewTagName";
        when(tagRepository.findByIdAndUser_Id(tag.getId(), user.getId())).thenReturn(Optional.of(tag));
        when(tagRepository.existsByUser_IdAndTagNameAndTagType(user.getId(), newTagName, tag.getTagType())).thenReturn(false);
        when(tagRepository.save(eq(tag))).thenReturn(tag);

        tagService.renameCustomTag(user.getId(), tag.getId(), newTagName);

        assertNotEquals(oldTagName, tag.getTagName(), "Tag should be renamed");
        assertEquals(newTagName, tag.getTagName(), "Tag should be renamed");
        verify(tagRepository, times(1)).findByIdAndUser_Id(tag.getId(), user.getId());
        verify(tagRepository, times(1)).existsByUser_IdAndTagNameAndTagType(
                eq(user.getId()), eq(newTagName), eq(TagType.CUSTOM));
        verify(tagRepository, times(1)).save(eq(tag));
    }

    @Test
    public void testDeleteCustomTagThrowsExceptionForNonExistingTag() {
        when(tagRepository.findByIdAndUser_Id(nonExistingId, user.getId()))
                .thenReturn(Optional.empty());

        RuntimeException exc = assertThrows(
                RuntimeException.class,
                () -> tagService.deleteCustomTag(user.getId(), nonExistingId)
        );

        assertEquals(tagNotFoundExceptionMessage, exc.getMessage(), "Unexpected exception message");

        verify(tagRepository, times(1))
                .findByIdAndUser_Id(nonExistingId, user.getId());
        verify(tagRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testDeleteCustomTagThrowsExceptionForNonCustomTag() {
        tag.setTagType(TagType.MOOD);

        when(tagRepository.findByIdAndUser_Id(tag.getId(), user.getId()))
                .thenReturn(Optional.of(tag));

        RuntimeException exc = assertThrows(
                RuntimeException.class,
                () -> tagService.deleteCustomTag(user.getId(), tag.getId())
        );

        String expectedExceptionMessage = "Can only delete custom tags";
        assertEquals(expectedExceptionMessage, exc.getMessage(), "Unexpected exception message");

        verify(tagRepository, times(1))
                .findByIdAndUser_Id(tag.getId(), user.getId());
        verify(tagRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testDeleteCustomTagDeletesTag() {
        tag.setTagType(TagType.CUSTOM);

        when(tagRepository.findByIdAndUser_Id(tag.getId(), user.getId()))
                .thenReturn(Optional.of(tag));

        tagService.deleteCustomTag(user.getId(), tag.getId());

        verify(tagRepository, times(1))
                .findByIdAndUser_Id(tag.getId(), user.getId());
        verify(tagRepository, times(1))
                .delete(eq(tag));
    }
}