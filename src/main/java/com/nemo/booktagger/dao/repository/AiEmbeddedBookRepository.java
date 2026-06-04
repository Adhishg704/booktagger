package com.nemo.booktagger.dao.repository;

import com.nemo.booktagger.entity.AiEmbeddedBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiEmbeddedBookRepository extends JpaRepository<AiEmbeddedBook, Integer> {
    Optional<AiEmbeddedBook> findByBook_Id(Integer bookId);

    boolean existsByBook_Id(Integer bookId);

    void deleteByBook_Id(Integer bookId);

    List<AiEmbeddedBook> findByBook_IdIn(List<Integer> bookIds);
}
