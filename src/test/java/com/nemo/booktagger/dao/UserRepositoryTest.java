package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        createUserData();
    }

    private void createUserData() {
        testUser = new User();
        testUser.setUsername("test");
        testUser.setEmail("test@example.com");

        userRepository.save(testUser);
    }

    @Test
    public void testFindUsernameByIdReturnsUsernameIfUserExists() {
        Optional<String> username = userRepository.findUsernameById(testUser.getId());
        assertTrue(username.isPresent());
        assertEquals("test", username.get(), "Unexpected username for test user");
    }

    @Test
    public void testFindUsernameByIdReturnsEmptyIfUserDoesNotExist() {
        Optional<String> username = userRepository.findUsernameById(Integer.MAX_VALUE);
        assertFalse(username.isPresent());
    }

    @Test
    public void testFindEmailByIdReturnsEmailIfUserExists() {
        Optional<String> email = userRepository.findEmailById(testUser.getId());
        assertTrue(email.isPresent());
        assertEquals("test@example.com", email.get(), "Unexpected email for test user");
    }

    @Test
    public void testFindEmailByIdReturnsEmptyIfUserDoesNotExist() {
        Optional<String> email = userRepository.findEmailById(Integer.MAX_VALUE);
        assertFalse(email.isPresent());
    }

    @Test
    public void testExistsByUsernameReturnsTrueIfUserExists() {
        assertTrue(userRepository.existsByUsername("test"));
    }

    @Test
    public void testExistsByUsernameReturnsFalseIfUserDoesNotExist() {
        assertFalse(userRepository.existsByUsername("test69420"));
    }

    @Test
    public void testExistsByEmailReturnsTrueIfUserExists() {
        assertTrue(userRepository.existsByEmail("test@example.com"));
    }

    @Test
    public void testExistsByEmailReturnsFalseIfUserDoesNotExist() {
        assertFalse(userRepository.existsByEmail("test69420@example.com"));
    }
}