package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    @Query("select b.title from Book b where b.id=:bookId")
    Optional<String> findTitleById(@Param("bookId") Integer bookId);

    @Query("select b.author from Book b where b.id=:bookId")
    Optional<String> findAuthorById(@Param("bookId") Integer bookId);

    @Query("select b.description from Book b where b.id=:bookId")
    Optional<String> findDescriptionById(@Param("bookId") Integer bookId);

    @Query("select b.isbn from Book b where b.id=:bookId")
    Optional<String> findIsbnById(@Param("bookId") Integer bookId);

    @Query("select b.yearPublished from Book b where b.id=:bookId")
    Optional<String> findYearPublishedById(@Param("bookId") Integer bookId);

    boolean existsByIsbn(String isbn);

    boolean existsByTitleAndAuthor(String title, String author);
}
