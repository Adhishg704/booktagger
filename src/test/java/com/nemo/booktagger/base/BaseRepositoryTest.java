package com.nemo.booktagger.base;

import com.nemo.booktagger.dao.BookRepository;
import com.nemo.booktagger.dao.BookTagRepository;
import com.nemo.booktagger.dao.TagRepository;
import com.nemo.booktagger.dao.UserBookRepository;
import com.nemo.booktagger.dao.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.factory.BookFactory;
import com.nemo.booktagger.factory.BookTagFactory;
import com.nemo.booktagger.factory.TagFactory;
import com.nemo.booktagger.factory.UserBookFactory;
import com.nemo.booktagger.factory.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BaseRepositoryTest {
    @Autowired
    protected UserBookRepository userBookRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected BookRepository bookRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    protected BookTagRepository bookTagRepository;

    protected User testUser;
    protected List<Book> testBooks;
    protected List<UserBook> testUserBooks;
    protected List<Tag> testTags;
    protected List<BookTag> testBookTags;

    protected void setUpUserBookRepositoryData() {
        createOneUserData();
        createBookData();
        createUserBookData();
    }

    protected void setUpUserRepositoryData() {
        createOneUserData();
    }

    protected void setUpBookTagRepositoryData() {
        createOneUserData();
        createBookData();
        createUserBookData();
        createTagData();
        createBookTagData();
    }

    private void createOneUserData() {
        testUser = UserFactory.createUser("test_1234", "test@example.com");
        userRepository.save(testUser);
    }

    private void createBookData() {
        testBooks = BookFactory.createBooks(10);
        bookRepository.saveAll(testBooks);
    }

    private void createUserBookData() {
        testUserBooks = UserBookFactory.createUserBooks(testUser, testBooks, 2025, ReadingStatus.READ, 5.0);
        userBookRepository.saveAll(testUserBooks);
    }

    private void createTagData() {
        testTags = TagFactory.createTagList(testUser, TagType.CUSTOM, "Fantasy", "Sci-fi", "Speculative fiction");
        tagRepository.saveAll(testTags);
    }

    private void createBookTagData() {
        testBookTags = BookTagFactory.createBookTagList(testUserBooks, testTags);
        bookTagRepository.saveAll(testBookTags);
    }
}
