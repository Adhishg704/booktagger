package com.nemo.booktagger.dao.specification;

import com.nemo.booktagger.base.BaseRepositoryTest;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserBookSpecificationsTest extends BaseRepositoryTest {

    private static final String YEAR_PUBLISHED = "2025";
    private static final String YEAR_READ = "2025";

    @BeforeEach
    public void setUp() {
        setUpUserBookRepositoryData();
    }

    @Test
    public void testGetBooksByPublishedYear() {
        Specification<UserBook> specification = Specification.where(UserBookSpecifications.hasUser(testUser.getId()));

        specification = specification.and(UserBookSpecifications.hasYearPublished(YEAR_PUBLISHED));

        List<UserBook> userBooks = userBookRepository.findAll(specification);

        assertEquals(testUserBooks.size(), userBooks.size(), "All books published in 2025");
    }

    @Test
    public void testGetBooksByReadYear() {
        Specification<UserBook> specification = Specification.where(UserBookSpecifications.hasUser(testUser.getId()));

        specification = specification.and(UserBookSpecifications.hasYearRead(YEAR_READ));

        List<UserBook> userBooks = userBookRepository.findAll(specification);

        assertEquals(testUserBooks.size(), userBooks.size(), "All books read in 2025");
    }

    @Test
    public void testGetBooksByReadStatus() {
        Specification<UserBook> specification = Specification.where(UserBookSpecifications.hasUser(testUser.getId()));

        specification = specification.and(UserBookSpecifications.hasStatus(ReadingStatus.READ));

        List<UserBook> userBooks = userBookRepository.findAll(specification);

        assertEquals(testUserBooks.size(), userBooks.size(), "All books read");
    }

    @Test
    public void testGetBooksByToReadStatus() {
        Specification<UserBook> specification = Specification.where(UserBookSpecifications.hasUser(testUser.getId()));

        specification = specification.and(UserBookSpecifications.hasStatus(ReadingStatus.TO_READ));

        List<UserBook> userBooks = userBookRepository.findAll(specification);

        assertEquals(0, userBooks.size(), "No books in TBR");
    }

    @Test
    public void testGetBooksByDNFStatus() {
        Specification<UserBook> specification = Specification.where(UserBookSpecifications.hasUser(testUser.getId()));

        specification = specification.and(UserBookSpecifications.hasStatus(ReadingStatus.DNF));

        List<UserBook> userBooks = userBookRepository.findAll(specification);

        assertEquals(0, userBooks.size(), "No books in DNF");
    }
}