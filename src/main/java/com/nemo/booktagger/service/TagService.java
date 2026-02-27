package com.nemo.booktagger.service;

import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.enums.TagType;

import java.util.List;

public interface TagService {

    Tag createTag(Integer userId, String tagName, TagType tagType);

    Tag getOrCreateTag(Integer userId, String tagName, TagType tagType);

    BookTag createBookTag(Integer userId, Integer bookId, Integer tagId);
    void renameCustomTag(Integer userId, Integer tagId, String newName);
    void deleteCustomTag(Integer userId, Integer tagId);

    List<String> getDistinctTagTypes(Integer userId);
}
