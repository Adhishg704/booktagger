package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.UserBookRepository;
import com.nemo.booktagger.dao.UserRepository;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserBookRepository userBookRepository;

    public UserServiceImpl(UserRepository userRepository, UserBookRepository userBookRepository) {
        this.userRepository = userRepository;
        this.userBookRepository = userBookRepository;
    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> userNotFound(userId));
    }

    @Override
    public String getUserNameById(Integer userId) {
        return userRepository.findUsernameById(userId)
                .orElseThrow(() -> userNotFound(userId));
    }

    @Override
    public String getEmailById(Integer userId) {
        return userRepository.findEmailById(userId)
                .orElseThrow(() -> userNotFound(userId));
    }

    @Override
    public User createUser(String username, String email) {
        if(!isUsernameValid(username)) {
            throw new IllegalArgumentException("Username is not valid");
        }
        if(!isEmailValid(email)) {
            throw new IllegalArgumentException("Email is not valid");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        userRepository.save(user);

        return user;
    }

    @Override
    @Transactional
    public User updateUserName(Integer userId, String newUsername) {
        if(!isUsernameValid(newUsername)) {
            throw new IllegalArgumentException("Username must be at least 6 characters and contain only letters, " +
                    "numbers, or underscores");
        }
        if(userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> userNotFound(userId));
        user.setUsername(newUsername);
        return user;
    }

    @Override
    @Transactional
    public User updateEmail(Integer userId, String newEmail) {
        if(!isEmailValid(newEmail)) {
            throw new IllegalArgumentException("Invalid email");
        }
        if(userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> userNotFound(userId));
        user.setEmail(newEmail);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> userNotFound(userId));
        userRepository.delete(user);
    }

    @Override
    public long countBooksInLibrary(Integer userId) {
        return userBookRepository.countByUser_Id(userId);
    }

    @Override
    public long countBooksInLibraryByStatus(Integer userId, ReadingStatus status) {
        return userBookRepository.countByUser_IdAndStatus(userId, status);
    }

    @Override
    public long countBooksInLibraryByYear(Integer userId, Integer year) {
        return userBookRepository.countByUser_IdAndYearRead(userId, year);
    }

    @Override
    public long countBooksInLibraryByYearAndStatus(Integer userId, ReadingStatus status, Integer year) {
        return userBookRepository.countByUser_IdAndStatusAndYearRead(userId, status, year);
    }

    private EntityNotFoundException userNotFound(Integer userId) {
        return new EntityNotFoundException("User id " + userId + " not found");
    }

    private boolean isUsernameValid(String newUsername) {
        return newUsername != null
                && !newUsername.isBlank()
                && newUsername.length() >= 6
                && newUsername.length() <= 50
                && newUsername.matches("[A-Za-z0-9_]+");
    }

    private boolean isEmailValid(String newEmail) {
        if(newEmail == null || newEmail.isBlank()) {
            return false;
        }
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return newEmail.matches(regex);
    }
}
