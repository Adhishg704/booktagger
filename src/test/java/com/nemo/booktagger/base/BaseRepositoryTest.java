package com.nemo.booktagger.base;

import com.nemo.booktagger.dao.BookRepository;
import com.nemo.booktagger.dao.UserBookRepository;
import com.nemo.booktagger.dao.UserRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.factory.BookFactory;
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

    protected User testUser;
    protected List<Book> testBooks;
    protected List<UserBook> testUserBooks;

    protected void setUpUserBookRepositoryData() {
        createOneUserData();
        createBookData();
        createUserBookData();
    }

    protected void setUpUserRepositoryData() {
        createOneUserData();
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
}
