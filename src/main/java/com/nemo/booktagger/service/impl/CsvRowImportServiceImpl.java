package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.service.BookService;
import com.nemo.booktagger.service.CsvRowImportService;
import com.nemo.booktagger.service.StoryGraphBookCsvRow;
import com.nemo.booktagger.service.TagService;
import com.nemo.booktagger.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CsvRowImportServiceImpl implements CsvRowImportService {
    private final UserService userService;
    private final BookService bookService;
    private final TagService tagService;

    public CsvRowImportServiceImpl(UserService userService, BookService bookService, TagService tagService) {
        this.userService = userService;
        this.bookService = bookService;
        this.tagService = tagService;
    }

    @Override
    @Transactional
    public void processRow(Integer userId, StoryGraphBookCsvRow row) {
        Book book = saveBook(row);
        saveUserBook(userId, book.getId(), row);
        saveMoodTags(userId, book.getId(), row);
        savePaceTag(userId, book.getId(), row);
    }

    private Book saveBook(StoryGraphBookCsvRow row) {
        String title = row.getTitle();
        String author = row.getAuthor();
        String isbn = row.getIsbn();

        return bookService.getOrCreateBook(title, author, isbn);
    }

    private void saveUserBook(Integer userId, Integer bookId, StoryGraphBookCsvRow row) {
        ReadingStatus status = parseReadingStatus(row);
        String yearRead = parseYearRead(row);
        double rating = row.getRating();

        bookService.addUserBook(userId, bookId, yearRead, status, rating);
    }

    private void saveMoodTags(Integer userId, Integer bookId, StoryGraphBookCsvRow row) {
        String moods = row.getMoods();
        if (moods == null || moods.isBlank()) {
            return;
        }

        String[] moodArray = Arrays.stream(moods.split(",")).
                map(String::trim).
                filter(s -> !s.isEmpty()).
                toArray(String[]::new);

        for(String mood: moodArray) {
            Tag moodTag = tagService.getOrCreateTag(userId, mood, TagType.MOOD);
            tagService.createBookTag(userId, bookId, moodTag.getId());
        }
    }

    private void savePaceTag(Integer userId, Integer bookId, StoryGraphBookCsvRow row) {
        String pace = row.getPace();
        if(pace == null || pace.isBlank()) {
            return;
        }

        Tag tag = tagService.getOrCreateTag(userId, pace, TagType.PACE);
        tagService.createBookTag(userId, bookId, tag.getId());
    }

    private ReadingStatus parseReadingStatus(StoryGraphBookCsvRow row) {
        ReadingStatus readingStatus;

        switch(row.getStatus()) {
            case "read":
                readingStatus = ReadingStatus.READ;
                break;
            case "to-read":
                readingStatus = ReadingStatus.TO_READ;
                break;
            case "did-not-finish":
                readingStatus = ReadingStatus.DNF;
                break;
            case "currently-reading":
                readingStatus = ReadingStatus.CURRENTLY_READING;
                break;
            default:
                throw new IllegalArgumentException("Unknown reading status: " + row.getStatus());
        }

        return readingStatus;
    }

    private String parseYearRead(StoryGraphBookCsvRow row) {
        String dateRead = row.getDateRead();
        if (dateRead != null && dateRead.length() >= 4) {
            return dateRead.substring(0, 4);
        }
        return null;
    }
}
