package com.nemo.booktagger.service;

import com.nemo.booktagger.entity.AiEmbeddedBook;

import java.util.List;
import java.util.Optional;

public interface AiEmbeddedBookService {

    AiEmbeddedBook save(AiEmbeddedBook embeddedBook);

    Optional<AiEmbeddedBook> getAiEmbeddedBook(Integer bookId);

    List<AiEmbeddedBook> getAiEmbeddedBooks(List<Integer> bookIds);
}
