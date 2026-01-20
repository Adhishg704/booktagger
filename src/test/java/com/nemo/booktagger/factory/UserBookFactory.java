package com.nemo.booktagger.factory;

import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;

import java.util.ArrayList;
import java.util.List;

public final class UserBookFactory {
    private UserBookFactory() {

    }

    public static UserBook createUserBook(User user, Book book, int yearRead, ReadingStatus status, double rating) {
        return new UserBook(user, book, yearRead, status, rating);
    }

    public static List<UserBook> createUserBooks(User user, List<Book> books, int yearRead, ReadingStatus status, double rating) {
        List<UserBook> userBooks = new ArrayList<>();

        for (Book book : books) {
            userBooks.add(createUserBook(user, book, yearRead, status, rating));
        }

        return userBooks;
    }
}
