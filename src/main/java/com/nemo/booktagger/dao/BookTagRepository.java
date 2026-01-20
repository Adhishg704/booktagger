package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Integer> {
    List<BookTag> findByUser_IdAndBook_Id(Integer userId, Integer bookId);
}
