package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.BookRepository;
import com.nemo.booktagger.dao.repository.BookTagRepository;
import com.nemo.booktagger.dao.repository.TagRepository;
import com.nemo.booktagger.dao.repository.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.exception.DuplicateResourceException;
import com.nemo.booktagger.exception.InvalidRequestException;
import com.nemo.booktagger.exception.ResourceNotFoundException;
import com.nemo.booktagger.factory.BookFactory;
import com.nemo.booktagger.factory.BookTagFactory;
import com.nemo.booktagger.factory.TagFactory;
import com.nemo.booktagger.factory.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceImplTest {
    private final Integer nonExistingId = 2;
    private final String tagName = "Fantasy";
    private final String tagNotFoundExceptionMessage = "Tag with tag id " + nonExistingId + " not found";
    private User user;
    private Book book;
    private Tag tag;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookTagRepository bookTagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    public void setUp() {
        user = UserFactory.createUser("user1", "user1@gmail.com");
        book = BookFactory.createBook("Book1", "Author1", "Desc1", "111111", "2025", "thumbnail");
        tag = TagFactory.createTag(user, TagType.CUSTOM, tagName);
    }

    @Test
    public void testCreateTagThrowsExceptionIfTagAlreadyExists() {
        when(tagRepository.existsByUser_IdAndTagNameAndTagType(user.getId(), tag.getTagName(), tag.getTagType()))
                .thenReturn(true);

        DuplicateResourceException exc = assertThrows(
                DuplicateResourceException.class,
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

        ResourceNotFoundException exc = assertThrows(
                ResourceNotFoundException.class,
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

        InvalidRequestException exc = assertThrows(
                InvalidRequestException.class,
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

        DuplicateResourceException exc = assertThrows(
                DuplicateResourceException.class,
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

        ResourceNotFoundException exc = assertThrows(
                ResourceNotFoundException.class,
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

        InvalidRequestException exc = assertThrows(
                InvalidRequestException.class,
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

    @Test
    public void testCreateBookTagThrowsExceptionForExistingBookTag() {
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);
        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        when(tagRepository.getReferenceById(tag.getId())).thenReturn(tag);
        when(bookTagRepository.existsByUser_IdAndBook_IdAndTag_Id(user.getId(), book.getId(), tag.getId())).thenReturn(true);

        DuplicateResourceException exc = assertThrows(
                DuplicateResourceException.class,
                () -> tagService.createBookTag(user.getId(), book.getId(), tag.getId())
        );

        String expectedExceptionMessage = "Tag already applied to user book";
        assertEquals(expectedExceptionMessage, exc.getMessage(), "Unexpected exception message");
        verify(userRepository, times(1)).getReferenceById(eq(user.getId()));
        verify(bookRepository, times(1)).getReferenceById(eq(book.getId()));
        verify(tagRepository, times(1)).getReferenceById(tag.getId());
        verify(bookTagRepository, times(1)).existsByUser_IdAndBook_IdAndTag_Id(
                user.getId(), book.getId(), tag.getId()
        );
        verify(bookTagRepository, never()).save(any(BookTag.class));
    }

    @Test
    public void testCreateBookTagCreatesBookTagForNonExistingBookTag() {
        BookTag bookTag = BookTagFactory.createBookTag(user, book, tag);
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);
        when(bookRepository.getReferenceById(book.getId())).thenReturn(book);
        when(tagRepository.getReferenceById(tag.getId())).thenReturn(tag);
        when(bookTagRepository.existsByUser_IdAndBook_IdAndTag_Id(user.getId(), book.getId(), tag.getId())).thenReturn(false);
        when(bookTagRepository.save(any(BookTag.class))).thenReturn(bookTag);

        BookTag returnedBookTag = tagService.createBookTag(user.getId(), book.getId(), tag.getId());

        assertNotNull(returnedBookTag, "Book tag must be returned");
        assertEquals(bookTag.getUser(), returnedBookTag.getUser(), "User must be same for both");
        assertEquals(bookTag.getBook(), returnedBookTag.getBook(), "Book must be same for both");
        assertEquals(bookTag.getTag(), returnedBookTag.getTag(), "Tag must be same for both");
        verify(userRepository, times(1)).getReferenceById(eq(user.getId()));
        verify(bookRepository, times(1)).getReferenceById(eq(book.getId()));
        verify(tagRepository, times(1)).getReferenceById(tag.getId());
        verify(bookTagRepository, times(1)).existsByUser_IdAndBook_IdAndTag_Id(
                user.getId(), book.getId(), tag.getId()
        );
        verify(bookTagRepository, times(1)).save(any(BookTag.class));
    }

    @Test
    public void testGetOrCreateTagReturnsExistingTagWithoutCreatingNewOne() {
        when(tagRepository.findByUser_IdAndTagNameAndTagType(user.getId(), tagName, tag.getTagType()))
                .thenReturn(Optional.of(tag));

        Tag result = tagService.getOrCreateTag(user.getId(), tagName, tag.getTagType());

        assertSame(tag, result, "Existing tag should be returned");
        verify(tagRepository, times(1)).findByUser_IdAndTagNameAndTagType(user.getId(), tagName, tag.getTagType());
        verify(userRepository, never()).getReferenceById(any());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    public void testGetOrCreateTagCreatesNewTagWhenNoneExists() {
        when(tagRepository.findByUser_IdAndTagNameAndTagType(user.getId(), tagName, tag.getTagType()))
                .thenReturn(Optional.empty());
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tag result = tagService.getOrCreateTag(user.getId(), tagName, tag.getTagType());

        assertNotNull(result, "A new tag should be created and returned");
        assertEquals(tagName, result.getTagName());
        assertEquals(tag.getTagType(), result.getTagType());
        assertEquals(user, result.getUser());
        verify(userRepository, times(1)).getReferenceById(user.getId());
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    public void testGetDistinctTagTypesMapsEnumsToDisplayNames() {
        when(tagRepository.findDistinctTagTypes(user.getId()))
                .thenReturn(List.of(TagType.MOOD, TagType.PACE, TagType.CUSTOM));

        List<String> result = tagService.getDistinctTagTypes(user.getId());

        assertEquals(List.of("Mood", "Pace", "Custom"), result, "Tag types should be mapped to display names");
        verify(tagRepository, times(1)).findDistinctTagTypes(user.getId());
    }

    @Test
    public void testGetDistinctTagTypesReturnsEmptyListWhenUserHasNoTags() {
        when(tagRepository.findDistinctTagTypes(user.getId())).thenReturn(List.of());

        List<String> result = tagService.getDistinctTagTypes(user.getId());

        assertTrue(result.isEmpty(), "Result should be empty when user has no tags");
    }
}