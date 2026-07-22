package com.nemo.booktagger.dao.repository;

import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.enums.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Integer>, JpaSpecificationExecutor<BookTag> {
    List<BookTag> findByUser_IdAndBook_Id(Integer userId, Integer bookId);
    List<BookTag> findByUser_IdAndTag_TagName(Integer userId, String tagName);
    boolean existsByUser_IdAndBook_IdAndTag_Id(Integer userId, Integer bookId, Integer tagId);

    @Query("""
            select distinct bt.tag.tagType
            from BookTag bt
            where bt.user.id=:userId
            """
    )
    List<TagType> getDistinctTagTypes(@Param("userId") Integer userId);

    @Query("""
    select distinct t.tagName
    from BookTag bt
    join bt.tag t
    where bt.user.id = :userId
      and t.tagType = :tagType
    order by t.tagName
    """)
    List<String> getDistinctTagNamesByUserIdAndTagType(
            @Param("userId") Integer userId,
            @Param("tagType") TagType tagType);
}
