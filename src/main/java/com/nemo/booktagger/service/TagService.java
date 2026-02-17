package com.nemo.booktagger.service;

import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.enums.TagType;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface TagService {

    Tag createTag(Integer userId, String tagName, TagType tagType);

    Tag getOrCreateTag(Integer userId, String tagName, TagType tagType);

    BookTag createBookTag(Integer userId, Integer bookId, Integer tagId);
    void renameCustomTag(Integer userId, Integer tagId, String newName);
    void deleteCustomTag(Integer userId, Integer tagId);
}
