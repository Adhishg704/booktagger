package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.enums.TagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    long countByUser_IdAndTagType(Integer userId, TagType tagType);

    List<Tag> findByUser_IdAndTagType(Integer userId, TagType tagType);
}
