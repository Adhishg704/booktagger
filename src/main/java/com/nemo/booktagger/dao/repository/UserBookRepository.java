package com.nemo.booktagger.dao.repository;

import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Integer>, JpaSpecificationExecutor<UserBook> {
    long countByUser_Id(Integer userId);

    long countByUser_IdAndYearRead(Integer userId, String year);

    long countByUser_IdAndStatus(Integer userId, ReadingStatus status);

    long countByUser_IdAndStatusAndYearRead(Integer userId, ReadingStatus status, String year);

    long countByBook_Id(Integer bookId);

    List<UserBook> findByUser_IdAndBook_YearPublished(
            Integer userId,
            String yearPublished
    );

    List<UserBook> findByUser_IdAndBook_Author(Integer userId, String author);

    List<UserBook> findByUser_IdAndYearRead(Integer userId, String year);

    List<UserBook> findByUser_IdAndStatus(Integer userId, ReadingStatus status);

    boolean existsByUser_IdAndBook_Id(Integer userId, Integer bookId);

    @Query(
            "select distinct ub.yearRead from UserBook ub where ub.user.id=:userId order by ub.yearRead desc"
    )
    List<String> findDistinctYearRead(@Param("userId") Integer userId);

    @Query("""
            select distinct ub.book.yearPublished
            from UserBook ub
            join ub.book b
            where ub.user.id = :userId
              and ub.status = :status
              and ub.book.yearPublished is not null
            order by ub.book.yearPublished desc
            """)
    List<String> findDistinctYearPublished(@Param("userId") Integer userId, @Param("status") ReadingStatus status);

    @Query("""
            select ub, bt
            from UserBook ub
            join fetch ub.book b
            left join BookTag bt on bt.user.id=ub.user.id
            and bt.book.id=b.id
            left join fetch bt.tag t
            where ub.user.id=:userId
            and ub.status=:status
            """)
    List<Object[]> getAllUserLibraryData(@Param("userId") Integer userId,
                                         @Param("status") ReadingStatus status);

    boolean existsByUser_IdAndBook_Isbn(Integer userId, String isbn);
}
