package com.nemo.booktagger.service;

import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.enums.TagType;

import java.util.List;
import java.util.Optional;

public interface TagService {
    Tag getTagById(Integer tagId);
    String getTagNameById(Integer tagId);
    TagType getTagTypeById(Integer tagId);

    List<BookTag> getBookTagsByTagName(Integer userId, String tagName);
    List<BookTag> getCustomBookTags(Integer userId);
    List<BookTag> getMoodBookTags(Integer userId);
    List<BookTag> getPaceBookTags(Integer userId);

    Tag createTag(Integer userId, String tagName, TagType tagType);
    void renameCustomTag(Integer userId, Integer tagId, String newName);
    void deleteCustomTag(Integer userId, Integer tagId);
}
