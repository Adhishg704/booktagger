package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.BookTagRepository;
import com.nemo.booktagger.dao.TagRepository;
import com.nemo.booktagger.dao.UserRepository;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final BookTagRepository bookTagRepository;

    public TagServiceImpl(TagRepository tagRepository, UserRepository userRepository, BookTagRepository bookTagRepository) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.bookTagRepository = bookTagRepository;
    }

    @Override
    public Tag getTagById(Integer tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> tagNotFound(tagId));
    }

    @Override
    public String getTagNameById(Integer tagId) {
        return tagRepository.findTagNameById(tagId)
                .orElseThrow(() -> tagNotFound(tagId));
    }

    @Override
    public TagType getTagTypeById(Integer tagId) {
        return tagRepository.findTagTypeById(tagId)
                .orElseThrow(() -> tagNotFound(tagId));
    }

    @Override
    public List<Tag> getCustomTagsCreatedByUser(Integer userId) {
        return tagRepository.findByUser_IdAndTagType(userId, TagType.CUSTOM);
    }

    @Override
    public List<Tag> getMoodTagsForUser(Integer userId) {
        return tagRepository.findByUser_IdAndTagType(userId, TagType.MOOD);
    }

    @Override
    public List<Tag> getPaceTagsForUser(Integer userId) {
        return tagRepository.findByUser_IdAndTagType(userId, TagType.PACE);
    }

    @Override
    public List<BookTag> getBookTagsByTagName(Integer userId, String tagName) {
        return bookTagRepository.findByUser_IdAndTag_TagName(userId, tagName);
    }

    @Override
    @Transactional
    public Tag createTag(Integer userId, String tagName, TagType tagType) {
        if(tagRepository.existsByUser_IdAndTagNameAndTagType(userId, tagName, tagType)) {
            throw new RuntimeException("Tag already exists");
        }

        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setTagType(tagType);
        
        User user = userRepository.getReferenceById(userId);
        tag.setUser(user);

        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void renameCustomTag(Integer userId, Integer tagId, String newName) {
        Tag tag = tagRepository.findByIdAndUser_Id(tagId, userId)
                .orElseThrow(() -> tagNotFound(tagId));

        TagType tagType = tag.getTagType();

        if(!tagType.equals(TagType.CUSTOM)) {
            throw new RuntimeException("Can only rename custom tags");
        }

        if(tagRepository.existsByUser_IdAndTagNameAndTagType(userId, newName, tagType)) {
            throw new RuntimeException("Tag with this name already exists");
        }

        tag.setTagName(newName);
        tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void deleteCustomTag(Integer userId, Integer tagId) {
        Tag tag = tagRepository.findByIdAndUser_Id(tagId, userId)
                .orElseThrow(() -> tagNotFound(tagId));

        TagType tagType = tag.getTagType();

        if(!tagType.equals(TagType.CUSTOM)) {
            throw new RuntimeException("Can only delete custom tags");
        }

        tagRepository.delete(tag);
    }

    private EntityNotFoundException tagNotFound(Integer tagId) {
        return new EntityNotFoundException("Tag with tag id " + tagId + " not found");
    }
}
