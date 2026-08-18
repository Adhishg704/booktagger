package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.AiEmbeddedBookRepository;
import com.nemo.booktagger.entity.AiEmbeddedBook;
import com.nemo.booktagger.exception.DuplicateResourceException;
import com.nemo.booktagger.service.AiEmbeddedBookService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AiEmbeddedBookServiceImpl implements AiEmbeddedBookService {

    private final AiEmbeddedBookRepository aiEmbeddedBookRepository;

    public AiEmbeddedBookServiceImpl(AiEmbeddedBookRepository aiEmbeddedBookRepository) {
        this.aiEmbeddedBookRepository = aiEmbeddedBookRepository;
    }

    @Override
    @Transactional
    public AiEmbeddedBook save(AiEmbeddedBook embeddedBook) {
        if(aiEmbeddedBookRepository.existsByBook_Id(embeddedBook.getBook().getId())) {
            throw new DuplicateResourceException("Book already exists");
        }
        return aiEmbeddedBookRepository.save(embeddedBook);
    }

    @Override
    public Optional<AiEmbeddedBook> getAiEmbeddedBook(Integer bookId) {
        return aiEmbeddedBookRepository.findByBook_Id(bookId);
    }

    @Override
    public List<AiEmbeddedBook> getAiEmbeddedBooks(List<Integer> bookIds) {
        return aiEmbeddedBookRepository.findByBook_IdIn(bookIds);
    }
}
