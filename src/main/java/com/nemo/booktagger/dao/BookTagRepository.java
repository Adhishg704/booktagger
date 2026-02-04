package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.enums.TagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Integer> {
    List<BookTag> findByUser_IdAndBook_Id(Integer userId, Integer bookId);
    List<BookTag> findByUser_IdAndTag_TagName(Integer userId, String tagName);
}
