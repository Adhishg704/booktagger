package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.rest.dto.response.common.UserLibraryCache;
import com.nemo.booktagger.service.mapper.UserLibraryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserLibraryCacheServiceImplTest {

    private static final Integer userId = 1;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private UserLibraryMapper userLibraryMapper;

    @InjectMocks
    private UserLibraryCacheServiceImpl userLibraryCacheService;

    @Test
    public void testGetUserLibraryMapsRepositoryDataUsingMapper() {
        List<Object[]> rawData = List.of(new Object[]{"row1"}, new Object[]{"row2"});
        UserBookDetailedResponse book = new UserBookDetailedResponse(1, "Title", "Author", "2025", "2025",
                "Desc", "thumb", 4.5, List.of("Fantasy"));
        when(userBookRepository.getAllUserLibraryData(userId, ReadingStatus.READ)).thenReturn(rawData);
        when(userLibraryMapper.toDetailedResponse(rawData)).thenReturn(List.of(book));

        UserLibraryCache result = userLibraryCacheService.getUserLibrary(userId);

        assertNotNull(result);
        assertEquals(List.of(book), result.books());
        verify(userBookRepository, times(1)).getAllUserLibraryData(userId, ReadingStatus.READ);
        verify(userLibraryMapper, times(1)).toDetailedResponse(rawData);
    }

    @Test
    public void testGetUserLibraryReturnsEmptyCacheWhenNoBooksExist() {
        List<Object[]> rawData = List.of();
        when(userBookRepository.getAllUserLibraryData(userId, ReadingStatus.READ)).thenReturn(rawData);
        when(userLibraryMapper.toDetailedResponse(rawData)).thenReturn(List.of());

        UserLibraryCache result = userLibraryCacheService.getUserLibrary(userId);

        assertTrue(result.books().isEmpty());
    }

    @Test
    public void testInvalidateDoesNotThrow() {
        assertDoesNotThrow(() -> userLibraryCacheService.invalidate(userId));
    }
}
