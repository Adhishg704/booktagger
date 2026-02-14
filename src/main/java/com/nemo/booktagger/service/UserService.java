package com.nemo.booktagger.service;

import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.enums.ReadingStatus;

import java.util.List;

public interface UserService {
    User getUserById(Integer userId);
    String getUserNameById(Integer userId);
    String getEmailById(Integer userId);

    User createUser(String username, String email);
    User updateUserName(Integer userId, String newUsername);
    User updateEmail(Integer userId, String newEmail);
    void deleteUser(Integer userId);

    long countBooksInLibrary(Integer userId);
    long countBooksInLibraryByStatus(Integer userId, ReadingStatus status);
    long countBooksInLibraryByYear(Integer userId, String year);
    long countBooksInLibraryByYearAndStatus(Integer userId, ReadingStatus status, String year);
}
