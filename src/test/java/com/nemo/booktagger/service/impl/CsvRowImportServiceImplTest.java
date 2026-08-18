package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.service.BookService;
import com.nemo.booktagger.service.StoryGraphBookCsvRow;
import com.nemo.booktagger.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CsvRowImportServiceImplTest {

    private int commonId = 10;

    private StoryGraphBookCsvRow row;

    @Mock
    private BookService bookService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private CsvRowImportServiceImpl csvRowImportService;

    @BeforeEach
    public void setUp() {
        row = new StoryGraphBookCsvRow();
    }

    @Test
    public void testCsvRowImportedForValidInput() {
        setRowFields(
                "Dune",
                "Frank Herbert",
                "123",
                "read",
                "2023-05-01",
                4.5,
                "dark, emotional",
                "fast"
        );
        Book book = mock(Book.class);
        Tag moodTag1 = mock(Tag.class);
        Tag moodTag2 = mock(Tag.class);
        Tag paceTag = mock(Tag.class);
        BookTag bookTag = mock(BookTag.class);
        when(book.getId()).thenReturn(commonId);
        when(moodTag1.getId()).thenReturn(commonId);
        when(moodTag2.getId()).thenReturn(commonId);
        when(paceTag.getId()).thenReturn(commonId);

        when(bookService.getOrCreateBook(row.getTitle(), row.getAuthor(), row.getIsbn())).thenReturn(book);
        when(tagService.getOrCreateTag(commonId, "dark", TagType.MOOD)).thenReturn(moodTag1);
        when(tagService.getOrCreateTag(commonId, "emotional", TagType.MOOD)).thenReturn(moodTag2);
        when(tagService.getOrCreateTag(commonId, "fast", TagType.PACE)).thenReturn(paceTag);
        when(tagService.createBookTag(commonId, commonId, commonId)).thenReturn(bookTag);

        csvRowImportService.processRow(commonId, row);

        verify(bookService).addUserBook(commonId, commonId, row.getDateRead().substring(0, 4),
                ReadingStatus.READ, row.getRating());
        verify(tagService, times(3)).createBookTag(commonId, commonId, commonId);
    }

    @Test
    public void testProcessRowSkipsEverythingWhenBookAlreadySavedForUser() {
        setRowFields("Dune", "Frank Herbert", "123", "read", "2023-05-01", 4.5, "dark", "fast");
        when(bookService.isBookAlreadySavedForUser(commonId, row.getIsbn())).thenReturn(true);

        csvRowImportService.processRow(commonId, row);

        verify(bookService, never()).getOrCreateBook(any(), any(), any());
        verify(bookService, never()).addUserBook(anyInt(), anyInt(), any(), any(), anyDouble());
        verifyNoInteractions(tagService);
    }

    @ParameterizedTest
    @CsvSource({
            "to-read, TO_READ",
            "did-not-finish, DNF",
            "currently-reading, CURRENTLY_READING"
    })
    public void testProcessRowMapsAllKnownReadingStatuses(String csvStatus, ReadingStatus expectedStatus) {
        setRowFields("Dune", "Frank Herbert", "123", csvStatus, "2023-05-01", 4.5, null, null);
        Book book = mock(Book.class);
        when(book.getId()).thenReturn(commonId);
        when(bookService.getOrCreateBook(row.getTitle(), row.getAuthor(), row.getIsbn())).thenReturn(book);

        csvRowImportService.processRow(commonId, row);

        verify(bookService).addUserBook(commonId, commonId, "2023", expectedStatus, row.getRating());
    }

    @Test
    public void testProcessRowThrowsExceptionForUnknownReadingStatus() {
        setRowFields("Dune", "Frank Herbert", "123", "some-unknown-status", "2023-05-01", 4.5, null, null);
        Book book = mock(Book.class);
        when(book.getId()).thenReturn(commonId);
        when(bookService.getOrCreateBook(row.getTitle(), row.getAuthor(), row.getIsbn())).thenReturn(book);

        assertThrows(IllegalArgumentException.class, () -> csvRowImportService.processRow(commonId, row));
    }

    @Test
    public void testProcessRowHandlesNullDateReadAsNullYearRead() {
        setRowFields("Dune", "Frank Herbert", "123", "to-read", null, 4.5, null, null);
        Book book = mock(Book.class);
        when(book.getId()).thenReturn(commonId);
        when(bookService.getOrCreateBook(row.getTitle(), row.getAuthor(), row.getIsbn())).thenReturn(book);

        csvRowImportService.processRow(commonId, row);

        verify(bookService).addUserBook(commonId, commonId, null, ReadingStatus.TO_READ, row.getRating());
    }

    @Test
    public void testProcessRowSkipsMoodAndPaceTagsWhenBlank() {
        setRowFields("Dune", "Frank Herbert", "123", "read", "2023-05-01", 4.5, "  ", "");
        Book book = mock(Book.class);
        when(book.getId()).thenReturn(commonId);
        when(bookService.getOrCreateBook(row.getTitle(), row.getAuthor(), row.getIsbn())).thenReturn(book);

        csvRowImportService.processRow(commonId, row);

        verifyNoInteractions(tagService);
    }

    @Test
    public void testProcessRowCreatesOnlyPaceTagWhenMoodsAreMissing() {
        setRowFields("Dune", "Frank Herbert", "123", "read", "2023-05-01", 4.5, null, "slow");
        Book book = mock(Book.class);
        Tag paceTag = mock(Tag.class);
        BookTag bookTag = mock(BookTag.class);
        when(book.getId()).thenReturn(commonId);
        when(paceTag.getId()).thenReturn(commonId);
        when(bookService.getOrCreateBook(row.getTitle(), row.getAuthor(), row.getIsbn())).thenReturn(book);
        when(tagService.getOrCreateTag(commonId, "slow", TagType.PACE)).thenReturn(paceTag);
        when(tagService.createBookTag(commonId, commonId, commonId)).thenReturn(bookTag);

        csvRowImportService.processRow(commonId, row);

        verify(tagService, times(1)).getOrCreateTag(commonId, "slow", TagType.PACE);
        verify(tagService, never()).getOrCreateTag(anyInt(), anyString(), eq(TagType.MOOD));
        verify(tagService, times(1)).createBookTag(commonId, commonId, commonId);
    }

    private void setRowFields(String title, String author, String isbn, String status, String dateRead,
                              double rating, String moods, String pace) {
        row.setTitle(title);
        row.setAuthor(author);
        row.setIsbn(isbn);
        row.setStatus(status);
        row.setDateRead(dateRead);
        row.setRating(rating);
        row.setMoods(moods);
        row.setPace(pace);
    }
}