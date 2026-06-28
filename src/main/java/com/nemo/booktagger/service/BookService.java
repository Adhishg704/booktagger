package com.nemo.booktagger.service;

import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.event.EnrichBookEvent;
import com.nemo.booktagger.rest.dto.response.ai.EnrichedBook;

import java.util.List;

public interface BookService {

    Book addBook(String title, String author, String isbn);

    Book getOrCreateBook(String title, String author, String isbn);

    Book getBookById(Integer bookId);

    Book getBookReferenceById(Integer bookId);

    UserBook addUserBook(Integer userId, Integer bookId, String yearRead, ReadingStatus status, Double rating);

    void embedBook(EnrichedBook enrichedBook, float[] vector);

    List<Integer> getSimilarBooksFromLibrary(Integer userId, float[] userQueryEmbedded);

    boolean isBookAlreadySavedForUser(Integer userId, String isbn);

    void enrichBook(EnrichBookEvent enrichBookEvent);
}
