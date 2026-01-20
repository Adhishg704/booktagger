package com.nemo.booktagger.dao;

import com.nemo.booktagger.base.BaseRepositoryTest;
import com.nemo.booktagger.enums.ReadingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserBookRepositoryTest extends BaseRepositoryTest {
    @BeforeEach
    public void setUp() {
        setUpUserBookRepositoryData();
    }

    @Test
    public void testCountByUserIdR() {
        assertEquals(
                10,
                userBookRepository.countByUser_Id(testUser.getId()),
                "Unexpected number of user books"
        );
    }

    @Test
    public void testCountByUserIdAndYearRead() {
        assertEquals(
                10,
                userBookRepository.countByUser_IdAndYearRead(testUser.getId(), 2025),
                "Unexpected number of user books in 2025"
        );
    }

    @Test
    public void testCountByUserIdAndStatus() {
        assertEquals(
                10,
                userBookRepository.countByUser_IdAndStatus(testUser.getId(), ReadingStatus.READ),
                "Unexpected number of user books read"
        );
    }

    @Test
    public void testCountByUserIdAndStatusAndYearRead() {
        assertEquals(
                10,
                userBookRepository.countByUser_IdAndStatusAndYearRead(testUser.getId(), ReadingStatus.READ, 2025),
                "Unexpected number of user books read in 2025"
        );
    }
}