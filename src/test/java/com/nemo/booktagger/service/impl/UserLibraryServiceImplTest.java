package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.BookTagRepository;
import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.enums.YearType;
import com.nemo.booktagger.factory.BookFactory;
import com.nemo.booktagger.factory.BookTagFactory;
import com.nemo.booktagger.factory.TagFactory;
import com.nemo.booktagger.factory.UserBookFactory;
import com.nemo.booktagger.factory.UserFactory;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.rest.dto.response.tags.TagDashboardResponse;
import com.nemo.booktagger.rest.dto.response.tags.TagNameGroup;
import com.nemo.booktagger.rest.dto.response.years.YearlyDashboardResponse;
import com.nemo.booktagger.service.UserLibraryCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserLibraryServiceImplTest {

    private final Integer userId = 1;
    private final Integer testBookSize = 2;

    private List<UserBook> testUserBooks;
    private List<BookTag> testBookTags;

    @Mock
    private UserLibraryCacheService userLibraryCacheService;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private BookTagRepository bookTagRepository;

    @InjectMocks
    private UserLibraryServiceImpl userLibraryService;

    @BeforeEach
    public void setUp() throws Exception {
        User testUser = UserFactory.createUser("user123", "user123@gmail.com");
        List<Book> books = BookFactory.createBooks(testBookSize);
        List<Tag> tags = TagFactory.createTagList(testUser, TagType.MOOD, "dark", "relaxing");

        testUserBooks = new ArrayList<>();
        testBookTags = new ArrayList<>();

        for (int i = 0; i < testBookSize; i++) {
            Book book = books.get(i);
            Field idField = Book.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(book, 100 + i);

            Tag tag = tags.get(i);

            UserBook userBook = UserBookFactory.createUserBook(testUser, book, "2025", ReadingStatus.READ, 5.0);
            BookTag bookTag = BookTagFactory.createBookTag(testUser, book, tag);

            testUserBooks.add(userBook);
            testBookTags.add(bookTag);
        }
    }

    // ----------------------------
    // getYearDashboardForUserLibrary
    // ----------------------------
    @Test
    public void testGetYearDashboardForUserLibrary_CacheHit() {
        List<Object[]> cacheData = new ArrayList<>();
        for (int i = 0; i < testBookSize; i++) {
            cacheData.add(new Object[]{testUserBooks.get(i), testBookTags.get(i)});
        }
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(cacheData);

        YearlyDashboardResponse response = userLibraryService.getYearDashboardForUserLibrary(userId);

        verify(userLibraryCacheService, times(1)).getUserLibrary(userId);
        assertEquals(1, response.byYearRead().size());
        assertTrue(response.byYearRead().containsKey("2025"));
        assertEquals(1, response.byYearPublished().size());
        assertTrue(response.byYearPublished().containsKey("2025"));
        List<String> tags = response.byYearRead().get("2025").stream()
                .flatMap(r -> r.tags().stream())
                .toList();
        assertTrue(tags.contains("dark"));
        assertTrue(tags.contains("relaxing"));
    }

    @Test
    public void testGetYearDashboardForUserLibrary_CacheMiss() {
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(Collections.emptyList());

        YearlyDashboardResponse response = userLibraryService.getYearDashboardForUserLibrary(userId);

        verify(userLibraryCacheService, times(1)).getUserLibrary(userId);
        assertTrue(response.byYearRead().isEmpty());
        assertTrue(response.byYearPublished().isEmpty());
    }

    // ----------------------------
    // getTagDashboardForUserLibrary
    // ----------------------------
    @Test
    public void testGetTagDashboardForUserLibrary_CacheHit() {
        List<Object[]> cacheData = new ArrayList<>();
        for (int i = 0; i < testBookSize; i++) {
            cacheData.add(new Object[]{testUserBooks.get(i), testBookTags.get(i)});
        }
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(cacheData);

        List<TagDashboardResponse> tagDashboard = userLibraryService.getTagDashboardForUserLibrary(userId);

        verify(userLibraryCacheService, times(1)).getUserLibrary(userId);
        assertEquals(1, tagDashboard.size());
        assertEquals("MOOD", tagDashboard.getFirst().tagType());
        List<String> tagNames = tagDashboard.getFirst().booksByTag().stream()
                .map(TagNameGroup::tagName)
                .toList();
        assertTrue(tagNames.contains("dark"));
        assertTrue(tagNames.contains("relaxing"));
    }

    @Test
    public void testGetTagDashboardForUserLibrary_CacheMiss() {
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(Collections.emptyList());

        List<TagDashboardResponse> tagDashboard = userLibraryService.getTagDashboardForUserLibrary(userId);

        verify(userLibraryCacheService, times(1)).getUserLibrary(userId);
        assertTrue(tagDashboard.isEmpty());
    }

    // ----------------------------
    // getYearsPublishedForUser
    // ----------------------------
    @Test
    public void testGetYearsPublishedForUser() {
        when(userBookRepository.findDistinctYearPublished(userId, ReadingStatus.READ))
                .thenReturn(List.of("2020", "2019"));

        List<String> yearsPublished = userLibraryService.getYearsPublishedForUser(userId);

        verify(userBookRepository, times(1)).findDistinctYearPublished(userId, ReadingStatus.READ);
        assertEquals(2, yearsPublished.size());
        assertTrue(yearsPublished.contains("2020"));
        assertTrue(yearsPublished.contains("2019"));
    }

    // ----------------------------
    // getYearsReadForUser
    // ----------------------------
    @Test
    public void testGetYearsReadForUser() {
        when(userBookRepository.findDistinctYearRead(userId))
                .thenReturn(List.of("2021", "2022"));

        List<String> yearsRead = userLibraryService.getYearsReadForUser(userId);

        verify(userBookRepository, times(1)).findDistinctYearRead(userId);
        assertEquals(2, yearsRead.size());
        assertTrue(yearsRead.contains("2021"));
        assertTrue(yearsRead.contains("2022"));
    }

    // ----------------------------
    // getTagTypesForUser
    // ----------------------------
    @Test
    public void testGetTagTypesForUser() {
        when(bookTagRepository.getDistinctTagTypes(userId))
                .thenReturn(List.of(TagType.MOOD, TagType.PACE));

        List<String> tagTypes = userLibraryService.getTagTypesForUser(userId);

        verify(bookTagRepository, times(1)).getDistinctTagTypes(userId);
        assertEquals(2, tagTypes.size());
        assertTrue(tagTypes.contains("MOOD"));
        assertTrue(tagTypes.contains("PACE"));
    }

    // ----------------------------
    // getTagNamesForUserForTagType
    // ----------------------------
    @Test
    public void testGetTagNamesForUserForTagType() {
        List<Object[]> cacheData = new ArrayList<>();
        for (int i = 0; i < testBookSize; i++) {
            cacheData.add(new Object[]{testUserBooks.get(i), testBookTags.get(i)});
        }
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(cacheData);

        List<String> tagNames = userLibraryService.getTagNamesForUserForTagType(userId, "MOOD");

        verify(userLibraryCacheService, times(1)).getUserLibrary(userId);
        assertTrue(tagNames.contains("dark"));
        assertTrue(tagNames.contains("relaxing"));
    }

    // ----------------------------
    // getUserBooksByYear
    // ----------------------------
    @Test
    public void testGetUserBooksByYear_Read() {
        List<Object[]> cacheData = new ArrayList<>();
        cacheData.add(new Object[]{testUserBooks.getFirst(), testBookTags.getFirst()});
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(cacheData);

        List<UserBookDetailedResponse> books = userLibraryService.getUserBooksByYear(userId, YearType.READ, "2025");

        verify(userLibraryCacheService, times(1)).getUserLibrary(userId);
        assertEquals(1, books.size());
        assertEquals(testUserBooks.getFirst().getBook().getTitle(), books.getFirst().title());
    }

    @Test
    public void testGetUserBooksByYear_Published() {
        List<Object[]> cacheData = new ArrayList<>();
        cacheData.add(new Object[]{testUserBooks.getFirst(), testBookTags.getFirst()});
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(cacheData);

        List<UserBookDetailedResponse> books = userLibraryService.getUserBooksByYear(userId, YearType.PUBLISHED, "2025");

        verify(userLibraryCacheService, times(1)).getUserLibrary(userId);
        assertEquals(1, books.size());
        assertEquals(testUserBooks.getFirst().getBook().getTitle(), books.getFirst().title());
    }

    // ----------------------------
    // getUserBooksByTagName
    // ----------------------------
    @Test
    public void testGetUserBooksByTagName() {
        List<Object[]> cacheData = new ArrayList<>();
        for (int i = 0; i < testBookSize; i++) {
            cacheData.add(new Object[]{testUserBooks.get(i), testBookTags.get(i)});
        }
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(cacheData);

        List<UserBookDetailedResponse> books = userLibraryService.getUserBooksByTagName(userId, "dark");

        verify(userLibraryCacheService, times(1)).getUserLibrary(userId);
        assertEquals(1, books.size());
        assertEquals(testUserBooks.getFirst().getBook().getTitle(), books.getFirst().title());
    }
}