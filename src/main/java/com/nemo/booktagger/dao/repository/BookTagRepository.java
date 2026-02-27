package com.nemo.booktagger.dao.repository;

import com.nemo.booktagger.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Integer>, JpaSpecificationExecutor<BookTag> {
    List<BookTag> findByUser_IdAndBook_Id(Integer userId, Integer bookId);
    List<BookTag> findByUser_IdAndTag_TagName(Integer userId, String tagName);
    boolean existsByUser_IdAndBook_IdAndTag_Id(Integer userId, Integer bookId, Integer tagId);
}
