package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.BookTagRepository;
import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.enums.YearType;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.rest.dto.response.common.UserLibraryCache;
import com.nemo.booktagger.service.UserLibraryCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserLibraryServiceImplTest {

    private static final Integer userId = 1;

    @Mock
    private UserLibraryCacheService userLibraryCacheService;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private BookTagRepository bookTagRepository;

    @InjectMocks
    private UserLibraryServiceImpl userLibraryService;

    private UserBookDetailedResponse book1;
    private UserBookDetailedResponse book2;

    @BeforeEach
    public void setUp() {
        book1 = new UserBookDetailedResponse(1, "Book1", "Author1", "2020", "2021",
                "Desc1", "thumb1", 4.5, List.of("Fantasy", "Dark"));
        book2 = new UserBookDetailedResponse(2, "Book2", "Author2", "2022", "2023",
                "Desc2", "thumb2", 3.5, List.of("Sci-fi"));
    }

    @Test
    public void testGetYearsPublishedForUserReturnsYearsFromRepository() {
        List<String> years = List.of("2020", "2021");
        when(userBookRepository.findDistinctYearPublished(userId, ReadingStatus.READ)).thenReturn(years);

        List<String> result = userLibraryService.getYearsPublishedForUser(userId);

        assertEquals(years, result, "Unexpected years published returned");
        verify(userBookRepository, times(1)).findDistinctYearPublished(userId, ReadingStatus.READ);
    }

    @Test
    public void testGetYearsReadForUserReturnsYearsFromRepository() {
        List<String> years = List.of("2022", "2023");
        when(userBookRepository.findDistinctYearRead(userId)).thenReturn(years);

        List<String> result = userLibraryService.getYearsReadForUser(userId);

        assertEquals(years, result, "Unexpected years read returned");
        verify(userBookRepository, times(1)).findDistinctYearRead(userId);
    }

    @Test
    public void testGetTagTypesForUserMapsEnumsToNames() {
        when(bookTagRepository.getDistinctTagTypes(userId)).thenReturn(List.of(TagType.MOOD, TagType.CUSTOM));

        List<String> result = userLibraryService.getTagTypesForUser(userId);

        assertEquals(List.of("MOOD", "CUSTOM"), result, "Tag types should be mapped to their enum names");
        verify(bookTagRepository, times(1)).getDistinctTagTypes(userId);
    }

    @Test
    public void testGetTagTypesForUserReturnsEmptyListWhenNoneExist() {
        when(bookTagRepository.getDistinctTagTypes(userId)).thenReturn(List.of());

        List<String> result = userLibraryService.getTagTypesForUser(userId);

        assertTrue(result.isEmpty(), "Result should be empty when user has no tags");
    }

    @Test
    public void testGetTagNamesForUserForTagTypeReturnsNamesFromRepository() {
        List<String> tagNames = List.of("Fantasy", "Sci-fi");
        when(bookTagRepository.getDistinctTagNamesByUserIdAndTagType(userId, TagType.CUSTOM)).thenReturn(tagNames);

        List<String> result = userLibraryService.getTagNamesForUserForTagType(userId, TagType.CUSTOM);

        assertEquals(tagNames, result, "Unexpected tag names returned");
        verify(bookTagRepository, times(1)).getDistinctTagNamesByUserIdAndTagType(userId, TagType.CUSTOM);
    }

    @Test
    public void testGetUserBooksByYearFiltersByYearReadWhenYearTypeIsRead() {
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(book1, book2)));

        List<UserBookDetailedResponse> result = userLibraryService.getUserBooksByYear(userId, YearType.READ, "2021");

        assertEquals(1, result.size(), "Only one book was read in 2021");
        assertEquals(book1, result.get(0), "Unexpected book returned");
    }

    @Test
    public void testGetUserBooksByYearFiltersByYearPublishedWhenYearTypeIsPublished() {
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(book1, book2)));

        List<UserBookDetailedResponse> result = userLibraryService.getUserBooksByYear(userId, YearType.PUBLISHED, "2022");

        assertEquals(1, result.size(), "Only one book was published in 2022");
        assertEquals(book2, result.get(0), "Unexpected book returned");
    }

    @Test
    public void testGetUserBooksByYearReturnsEmptyListWhenNoBooksMatchYear() {
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(book1, book2)));

        List<UserBookDetailedResponse> result = userLibraryService.getUserBooksByYear(userId, YearType.READ, "1999");

        assertTrue(result.isEmpty(), "No books should match a year with no entries");
    }

    @Test
    public void testGetUserBooksByYearReturnsEmptyListForEmptyLibrary() {
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of()));

        List<UserBookDetailedResponse> result = userLibraryService.getUserBooksByYear(userId, YearType.READ, "2021");

        assertTrue(result.isEmpty(), "Result should be empty for a user with no library data");
    }

    @Test
    public void testGetUserBooksByTagNameFiltersBooksContainingTag() {
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(book1, book2)));

        List<UserBookDetailedResponse> result = userLibraryService.getUserBooksByTagName(userId, "Fantasy");

        assertEquals(1, result.size(), "Only one book has the Fantasy tag");
        assertEquals(book1, result.get(0), "Unexpected book returned");
    }

    @Test
    public void testGetUserBooksByTagNameReturnsEmptyListWhenNoBookHasTag() {
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of(book1, book2)));

        List<UserBookDetailedResponse> result = userLibraryService.getUserBooksByTagName(userId, "Nonexistent");

        assertTrue(result.isEmpty(), "No books should match an unused tag");
    }

    @Test
    public void testGetUserBooksByTagNameReturnsEmptyListForEmptyLibraryWithoutThrowing() {
        when(userLibraryCacheService.getUserLibrary(userId)).thenReturn(new UserLibraryCache(List.of()));

        List<UserBookDetailedResponse> result = userLibraryService.getUserBooksByTagName(userId, "Fantasy");

        assertTrue(result.isEmpty(), "Result should be empty for a user with no library data, without throwing");
    }
}
