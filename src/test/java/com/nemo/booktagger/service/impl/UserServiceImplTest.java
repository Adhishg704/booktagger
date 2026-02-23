package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.dao.repository.UserRepository;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.enums.ReadingStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private final Integer userId = 1;
    private final Integer nonExistingId = 2;
    private final String userNotFoundExceptionMessage = "User id " + nonExistingId + " not found";
    private User user;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUsername("test_1234");
        user.setEmail("test@example.com");
    }

    @Test
    public void testGetUserByIdReturnsUserForExistingUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User returnedUser = userService.getUserById(userId);

        assertNotNull(returnedUser);
        assertEquals(user.getUsername(), returnedUser.getUsername(), "Usernames must match");
        assertEquals(user.getEmail(), returnedUser.getEmail(), "Emails must match");
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testGetUserByIdThrowsExceptionForNonExistingUser() {
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            userService.getUserById(nonExistingId);
        });
        assertEquals(userNotFoundExceptionMessage, ex.getMessage(),
                "Unexpected exception message");
        verify(userRepository, times(1)).findById(nonExistingId);
    }

    @Test
    public void testGetUsernameByIdReturnsUsernameForExistingUser() {
        when(userRepository.findUsernameById(userId)).thenReturn(Optional.of(user.getUsername()));

        String returnedUsername = userService.getUserNameById(userId);

        assertNotNull(returnedUsername);
        assertEquals(user.getUsername(), returnedUsername, "Usernames do not match");
        verify(userRepository, times(1)).findUsernameById(userId);
    }

    @Test
    public void testGetUsernameByIdThrowsExceptionForNonExistingUser() {
        when(userRepository.findUsernameById(nonExistingId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            userService.getUserNameById(nonExistingId);
        });
        assertEquals(userNotFoundExceptionMessage, ex.getMessage(), "Unexpected exception message");
        verify(userRepository, times(1)).findUsernameById(nonExistingId);
    }

    @Test
    public void testGetEmailByIdReturnsEmailForExistingUser() {
        when(userRepository.findEmailById(userId))
                .thenReturn(Optional.of(user.getEmail()));

        String returnedEmail = userService.getEmailById(userId);

        assertNotNull(returnedEmail);
        assertEquals(user.getEmail(), returnedEmail, "Emails do not match");
        verify(userRepository, times(1)).findEmailById(userId);
    }

    @Test
    public void testGetEmailByIdThrowsExceptionForNonExistingUser() {
        when(userRepository.findEmailById(nonExistingId))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getEmailById(nonExistingId)
        );

        assertEquals(userNotFoundExceptionMessage, ex.getMessage(), "Unexpected exception message");
        verify(userRepository, times(1)).findEmailById(nonExistingId);
    }

    @ParameterizedTest
    @ValueSource(strings = { "abcd", "abcd$$%" })
    public void testUpdateUserNamethrowsExceptionForInvalidUsername(String invalidUsername) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserName(userId, invalidUsername)
        );

        assertEquals(
                "Username must be at least 6 characters and contain only letters, numbers, or underscores",
                ex.getMessage()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    public void testUpdateUsernameThrowsExceptionForExistingUserName() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserName(userId, user.getUsername())
        );

        assertEquals(
                "Username is already taken",
                ex.getMessage()
        );
        verify(userRepository, times(1)).existsByUsername(user.getUsername());
    }

    @Test
    public void testUpdateUsernameThrowsExceptionForNonExistingUser() {
        String newUsername = "helloworld_123";
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUserName(nonExistingId, newUsername)
        );

        assertEquals(
                userNotFoundExceptionMessage,
                ex.getMessage()
        );
        verify(userRepository, times(1)).existsByUsername(newUsername);
        verify(userRepository, times(1)).findById(nonExistingId);
    }

    @Test
    public void testUpdateUsernameReturnsUserForValidUsernameAndExistingUser() {
        String newUsername = "helloworld_123";
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User updatedUser = userService.updateUserName(userId, newUsername);

        assertNotNull(updatedUser);
        assertEquals(newUsername, updatedUser.getUsername(), "Username not updated");
        verify(userRepository, times(1)).existsByUsername(newUsername);
        verify(userRepository, times(1)).findById(userId);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abcd",                 // no @
            "abcd@",                // no domain
            "@example.com",         // no local part
            "abcd@example",         // no TLD
            "abcd@example.",        // dot but no TLD
            "abcd@.com",            // domain starts with dot
            "ab cd@example.com",    // space in local part
            "abcd@@example.com",    // double @
            "abcd@example..com",    // double dot in domain
            "abcd$%*@example.com"   // invalid characters
    })
    public void testUpdateEmailthrowsExceptionForInvalidEmail(String invalidEmail) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateEmail(userId, invalidEmail)
        );

        assertEquals(
                "Invalid email",
                ex.getMessage()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    public void testUpdateEmailThrowsExceptionForExistingEmail() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateEmail(userId, user.getEmail())
        );

        assertEquals(
                "Email already exists",
                ex.getMessage()
        );
        verify(userRepository, times(1)).existsByEmail(user.getEmail());
    }

    @Test
    public void testUpdateEmailReturnsUserForValidEmailAndExistingUser() {
        String newEmail = "test1@example.com";
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User updatedUser = userService.updateEmail(userId, newEmail);

        assertNotNull(updatedUser);
        assertEquals(newEmail, updatedUser.getEmail(), "Email not updated");
        verify(userRepository, times(1)).existsByEmail(newEmail);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testDeleteUserThrowsExceptionForNonExistingUser() {
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.deleteUser(nonExistingId)
        );

        assertEquals(userNotFoundExceptionMessage, ex.getMessage(), "Unexpected exception message");
        verify(userRepository, times(1)).findById(nonExistingId);
        verify(userRepository, never()).delete(user);
    }

    @Test
    public void testDeleteUserDeletesForExistingUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void testCountBooksInLibraryReturnsCorrectValue() {
        long expectedCount = 5L;
        when(userBookRepository.countByUser_Id(userId)).thenReturn(expectedCount);

        long actualCount = userService.countBooksInLibrary(userId);

        assertEquals(expectedCount, actualCount, "Book count should match repository result");
        verify(userBookRepository, times(1)).countByUser_Id(userId);
    }

    @Test
    public void testCountBooksInLibraryByStatusReturnsCorrectValue() {
        long expectedCount = 3L;
        ReadingStatus status = ReadingStatus.READ;

        when(userBookRepository.countByUser_IdAndStatus(userId, status)).thenReturn(expectedCount);

        long actualCount = userService.countBooksInLibraryByStatus(userId, status);

        assertEquals(expectedCount, actualCount, "Book count for status should match repository result");
        verify(userBookRepository, times(1)).countByUser_IdAndStatus(userId, status);
    }

    @Test
    public void testCountBooksInLibraryByYearReturnsCorrectValue() {
        long expectedCount = 2L;
        String year = "2025";

        when(userBookRepository.countByUser_IdAndYearRead(userId, year)).thenReturn(expectedCount);

        long actualCount = userService.countBooksInLibraryByYear(userId, year);

        assertEquals(expectedCount, actualCount, "Book count for year should match repository result");
        verify(userBookRepository, times(1)).countByUser_IdAndYearRead(userId, year);
    }

    @Test
    public void testCountBooksInLibraryByYearAndStatusReturnsCorrectValue() {
        long expectedCount = 1L;
        ReadingStatus status = ReadingStatus.READ;
        String year = "2025";

        when(userBookRepository.countByUser_IdAndStatusAndYearRead(userId, status, year))
                .thenReturn(expectedCount);

        long actualCount = userService.countBooksInLibraryByYearAndStatus(userId, status, year);

        assertEquals(expectedCount, actualCount, "Book count for year and status should match repository result");
        verify(userBookRepository, times(1))
                .countByUser_IdAndStatusAndYearRead(userId, status, year);
    }
}