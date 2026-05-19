package com.nemo.booktagger.dao.repository;

import com.nemo.booktagger.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query(value = """
        SELECT b.id
        FROM books b
        JOIN user_books ub ON ub.book_id = b.id
        WHERE ub.user_id = :userId
        ORDER BY b.embedding <=> CAST(:queryVector AS vector)
        LIMIT 10
        """, nativeQuery = true)
    List<Integer> retrieveBookIdsSimilarToUserQuery(
            @Param("userId") Integer userId,
            @Param("queryVector") float[] queryVector
    );

    Optional<Book> findByIsbn(String isbn);

    Optional<Book> findByTitleAndAuthor(String title, String author);

    boolean existsByIsbn(String isbn);

    boolean existsByTitleAndAuthor(String title, String author);
}
