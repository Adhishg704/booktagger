package com.nemo.booktagger.service;

import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;

import java.util.List;

public interface BookService {

    Book getBookById(Integer bookId);
    String getTitleById(Integer bookId);
    String getAuthorById(Integer bookId);
    String getDescriptionById(Integer bookId);
    String getIsbnById(Integer bookId);
    String getYearPublishedById(Integer bookId);

    Book addBook(String title, String author, String description, String isbn, String yearPublished);
    UserBook addUserBook(Integer userId, Integer bookId, Integer yearRead, ReadingStatus status, Double rating);

    long getNumberOfUsersWhoOwnTheBook(Integer bookId);
    List<BookTag> getTagsAssociatedWithBookForUser(Integer userId, Integer bookId);

    List<UserBook> getUserBooksByAuthor(Integer userId, String author);
    List<UserBook> getUserBooksByTitle(Integer userId, String titlePart);
    List<UserBook> getUserBooksByYearPublished(Integer userId, String year);
    List<UserBook> getUserBooksByYearRead(Integer userId, Integer year);
    List<UserBook> getUserBooksByRatingRange(Integer userId, double minRating, double maxRating);
    List<UserBook> getBooksReadByUser(Integer userId);
    List<UserBook> getBooksUserDidNotFinish(Integer userId);
    List<UserBook> getBooksUserWantsToRead(Integer userId);

    UserBook updateUserBookStatus(Integer userId, Integer bookId, ReadingStatus status);
    UserBook updateUserBookRating(Integer userId, Integer bookId, double rating);
    void deleteUserBook(Integer userId, Integer bookId);
}
