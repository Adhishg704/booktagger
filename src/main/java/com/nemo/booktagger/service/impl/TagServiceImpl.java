package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.BookRepository;
import com.nemo.booktagger.dao.repository.BookTagRepository;
import com.nemo.booktagger.dao.repository.TagRepository;
import com.nemo.booktagger.dao.repository.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookTagRepository bookTagRepository;

    public TagServiceImpl(TagRepository tagRepository, UserRepository userRepository, BookRepository bookRepository,
                          BookTagRepository bookTagRepository) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookTagRepository = bookTagRepository;
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

    @Transactional
    @Override
    public Tag getOrCreateTag(Integer userId, String tagName, TagType tagType) {
        return tagRepository.findByUser_IdAndTagNameAndTagType(userId, tagName, tagType).
                orElseGet(() -> {
                    Tag tag = new Tag();
                    tag.setTagName(tagName);
                    tag.setTagType(tagType);
                    tag.setUser(userRepository.getReferenceById(userId));
                    return tagRepository.save(tag);
                });
    }

    @Override
    @Transactional
    public BookTag createBookTag(Integer userId, Integer bookId, Integer tagId) {
        User user = userRepository.getReferenceById(userId);
        Book book = bookRepository.getReferenceById(bookId);
        Tag tag = tagRepository.getReferenceById(tagId);

        if(bookTagRepository.existsByUser_IdAndBook_IdAndTag_Id(userId, bookId, tagId)) {
            throw new RuntimeException("Tag already applied to user book");
        }

        BookTag bookTag = new BookTag();
        bookTag.setUser(user);
        bookTag.setBook(book);
        bookTag.setTag(tag);

        return bookTagRepository.save(bookTag);
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

    @Override
    public List<String> getDistinctTagTypes(Integer userId) {
        return tagRepository.findDistinctTagTypes(userId)
                .stream().map(this::mapTagTypeToString)
                .toList();
    }

    private String mapTagTypeToString(TagType tagType) {
        return switch (tagType) {
            case TagType.MOOD -> "Mood";
            case TagType.PACE -> "Pace";
            case TagType.CUSTOM -> "Custom";
        };
    }

    private EntityNotFoundException tagNotFound(Integer tagId) {
        return new EntityNotFoundException("Tag with tag id " + tagId + " not found");
    }
}
