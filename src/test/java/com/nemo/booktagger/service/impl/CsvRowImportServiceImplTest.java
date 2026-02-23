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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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