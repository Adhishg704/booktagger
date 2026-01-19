package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Integer> {
    long countByUser_Id(Integer userId);

    long countByUser_IdAndYearRead(Integer userId, Integer year);

    long countByUser_IdAndStatus(Integer userId, ReadingStatus status);

    long countByUser_IdAndStatusAndYearRead(Integer userId, ReadingStatus status, Integer year);
}
