package com.nemo.booktagger.dao.repository;

import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.enums.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    @Query("select t.tagName from Tag t where t.id=:tagId")
    Optional<String> findTagNameById(@Param("tagId") Integer tagId);

    @Query("select t.tagType from Tag t where t.id=:tagId")
    Optional<TagType> findTagTypeById(@Param("tagId") Integer tagId);

    Optional<Tag> findByIdAndUser_Id(Integer tagId, Integer userId);

    List<Tag> findByUser_IdAndTagType(Integer userId, TagType tagType);

    Optional<Tag> findByUser_IdAndTagNameAndTagType(Integer userId, String tagName, TagType tagType);

    boolean existsByUser_IdAndTagNameAndTagType(Integer userId, String tagName, TagType tagType);
}
