package com.nemo.booktagger.dao;

import com.nemo.booktagger.base.BaseRepositoryTest;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
    public void testCountByUserId() {
        assertEquals(
                testUserBooks.size(),
                userBookRepository.countByUser_Id(testUser.getId()),
                "Unexpected number of user books"
        );
    }

    @Test
    public void testCountByUserIdAndYearRead() {
        assertEquals(
                testUserBooks.size(),
                userBookRepository.countByUser_IdAndYearRead(testUser.getId(), 2025),
                "Unexpected number of user books in 2025"
        );
    }

    @Test
    public void testCountByUserIdAndStatus() {
        assertEquals(
                testUserBooks.size(),
                userBookRepository.countByUser_IdAndStatus(testUser.getId(), ReadingStatus.READ),
                "Unexpected number of user books read"
        );
    }

    @Test
    public void testCountByUserIdAndStatusAndYearRead() {
        assertEquals(
                testUserBooks.size(),
                userBookRepository.countByUser_IdAndStatusAndYearRead(testUser.getId(), ReadingStatus.READ, 2025),
                "Unexpected number of user books read in 2025"
        );
    }

    @Test
    public void testCountByBookId() {
        Book firstBook = testBooks.getFirst();
        assertEquals(
                1,
                userBookRepository.countByBook_Id(firstBook.getId()),
                "Expected only one user to own book"
        );
    }

    @Test
    public void testFindByUserIdAndBookYearPublished() {
        Book firstBook = testBooks.getFirst();
        List<UserBook> userBooks = userBookRepository.findByUser_IdAndBook_YearPublished(testUser.getId(),
                firstBook.getYearPublished());

        assertEquals(testUserBooks.size(), userBooks.size(), "User should own all the books released in " +
                firstBook.getYearPublished());
        userBooks.forEach(userBook -> {
            assertEquals(
                    testUser.getId(),
                    userBook.getUser().getId(),
                    "User id mismatch"
            );

            assertEquals(
                    firstBook.getYearPublished(),
                    userBook.getBook().getYearPublished(),
                    "Year published mismatch"
            );
        });

    }

    @Test
    public void testFindByUserIdAndYearRead() {
        Integer yearRead = testUserBooks.getFirst().getYearRead();
        List<UserBook> userBooks = userBookRepository.findByUser_IdAndYearRead(testUser.getId(),
                yearRead);

        assertEquals(testUserBooks.size(), userBooks.size(), "All test books read in " + yearRead);
        userBooks.forEach(userBook -> {
            assertEquals(
                    testUser.getId(),
                    userBook.getUser().getId(),
                    "User id mismatch"
            );

            assertEquals(
                    yearRead,
                    userBook.getYearRead(),
                    "Year published mismatch"
            );
        });
    }

    @Test
    public void testFindByUserIdAndStatus() {
        List<UserBook> userBooksRead = userBookRepository.findByUser_IdAndStatus(testUser.getId(),
                ReadingStatus.READ);
        List<UserBook> userBooksTBR = userBookRepository.findByUser_IdAndStatus(testUser.getId(),
                ReadingStatus.TO_READ);
        List<UserBook> userBooksDNF= userBookRepository.findByUser_IdAndStatus(testUser.getId(),
                ReadingStatus.DNF);

        assertEquals(testUserBooks.size(), userBooksRead.size(), "All user books read");
        assertEquals(0, userBooksTBR.size(), "No user books in TBR");
        assertEquals(0, userBooksDNF.size(), "No user books in DNF");
        userBooksRead.forEach(userBook -> {
            assertEquals(
                    testUser.getId(),
                    userBook.getUser().getId(),
                    "User id mismatch"
            );

            assertEquals(
                    ReadingStatus.READ,
                    userBook.getStatus(),
                    "Status mismatch"
            );
        });
    }

    @Test
    public void testFindByUserIdAndBookAuthor() {
        Book firstBook = testBooks.getFirst();
        List<UserBook> userBooks = userBookRepository.findByUser_IdAndBook_Author(testUser.getId(),
                firstBook.getAuthor());

        assertEquals(1, userBooks.size(), "User should have exactly one book by this author");
        Book userbook = userBooks.getFirst().getBook();
        assertEquals(firstBook.getAuthor(), userbook.getAuthor(), "Unexpected author");
    }
}