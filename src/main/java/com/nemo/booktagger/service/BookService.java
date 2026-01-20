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
    long getIsbnById(Integer bookId);
    Integer getYearPublishedById(Integer bookId);

    Book addBook(String title, String author, String description, long isbn, Integer yearPublished);

    long getNumberOfUsersWhoOwnTheBook(Integer bookId);
    List<UserBook> getUserBooksPublishedInAParticularYear(Integer userId, Integer yearPublished);
    List<BookTag> getTagsAssociatedWithBookForUser(Integer userId, Integer bookId);

    List<UserBook> getUserBooksByAuthor(Integer userId, String author);
    List<UserBook> getUserBooksByTitle(Integer userId, String titlePart);
    List<UserBook> getUserBooksByYearPublished(Integer userId, Integer year);
    List<UserBook> getUserBooksByYearRead(Integer userId, Integer year);
    List<UserBook> getUserBooksByRatingRange(Integer userId, double minRating, double maxRating);
    List<UserBook> getBooksReadByUser(Integer userId);
    List<UserBook> getBooksUserDidNotFinish(Integer userId);
    List<UserBook> getBooksUserWantsToRead(Integer userId);

    UserBook updateUserBookStatus(Integer userId, Integer bookId, ReadingStatus status);
    UserBook updateUserBookRating(Integer userId, Integer bookId, double rating);
    void deleteUserBook(Integer userId, Integer bookId);
}
