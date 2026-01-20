package com.nemo.booktagger.service;

import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.UserBook;

import java.util.List;

public interface BookService {
    Book getBookById(Integer bookId);
    String getTitleById(Integer bookId);
    String getAuthorById(Integer bookId);
    String getDescriptionById(Integer bookId);
    long getIsbnById(Integer bookId);
    Integer getYearPublishedById(Integer bookId);
    double getRatingById(Integer bookId);

    Book addBook(String title, String author, String description, long isbn, Integer yearPublished, double rating);
    long getNumberOfUsersWhoOwnTheBook(Integer bookId);
    List<UserBook> getUserBooksPublishedInAParticularYear(Integer userId, Integer yearPublished);
    List<BookTag> getTagsAssociatedWithBookForUser(Integer userId, Integer bookId);
}
