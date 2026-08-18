package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.AiEmbeddedBookRepository;
import com.nemo.booktagger.entity.AiEmbeddedBook;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.exception.DuplicateResourceException;
import com.nemo.booktagger.factory.BookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiEmbeddedBookServiceImplTest {

    private Book book;
    private AiEmbeddedBook aiEmbeddedBook;

    @Mock
    private AiEmbeddedBookRepository aiEmbeddedBookRepository;

    @InjectMocks
    private AiEmbeddedBookServiceImpl aiEmbeddedBookService;

    @BeforeEach
    public void setUp() {
        book = BookFactory.createBook("Book1", "Author1", "Desc1", "111111", "2025", "thumbnail");
        aiEmbeddedBook = new AiEmbeddedBook(book, "Book1", "Author1", "Summary", "fantasy", "loss", "dark", "dragon");
    }

    @Test
    public void testSaveThrowsExceptionWhenBookAlreadyEmbedded() {
        when(aiEmbeddedBookRepository.existsByBook_Id(book.getId())).thenReturn(true);

        DuplicateResourceException exc = assertThrows(
                DuplicateResourceException.class,
                () -> aiEmbeddedBookService.save(aiEmbeddedBook)
        );

        assertEquals("Book already exists", exc.getMessage());
        verify(aiEmbeddedBookRepository, times(1)).existsByBook_Id(book.getId());
        verify(aiEmbeddedBookRepository, never()).save(any(AiEmbeddedBook.class));
    }

    @Test
    public void testSaveSavesAndReturnsEmbeddedBookWhenNotAlreadyEmbedded() {
        when(aiEmbeddedBookRepository.existsByBook_Id(book.getId())).thenReturn(false);
        when(aiEmbeddedBookRepository.save(aiEmbeddedBook)).thenReturn(aiEmbeddedBook);

        AiEmbeddedBook result = aiEmbeddedBookService.save(aiEmbeddedBook);

        assertSame(aiEmbeddedBook, result);
        verify(aiEmbeddedBookRepository, times(1)).existsByBook_Id(book.getId());
        verify(aiEmbeddedBookRepository, times(1)).save(aiEmbeddedBook);
    }

    @Test
    public void testGetAiEmbeddedBookReturnsPresentOptionalWhenFound() {
        when(aiEmbeddedBookRepository.findByBook_Id(book.getId())).thenReturn(Optional.of(aiEmbeddedBook));

        Optional<AiEmbeddedBook> result = aiEmbeddedBookService.getAiEmbeddedBook(book.getId());

        assertTrue(result.isPresent());
        assertSame(aiEmbeddedBook, result.get());
    }

    @Test
    public void testGetAiEmbeddedBookReturnsEmptyOptionalWhenNotFound() {
        when(aiEmbeddedBookRepository.findByBook_Id(999)).thenReturn(Optional.empty());

        Optional<AiEmbeddedBook> result = aiEmbeddedBookService.getAiEmbeddedBook(999);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAiEmbeddedBooksDelegatesToRepository() {
        List<Integer> bookIds = List.of(1, 2, 3);
        List<AiEmbeddedBook> expected = List.of(aiEmbeddedBook);
        when(aiEmbeddedBookRepository.findByBook_IdIn(bookIds)).thenReturn(expected);

        List<AiEmbeddedBook> result = aiEmbeddedBookService.getAiEmbeddedBooks(bookIds);

        assertEquals(expected, result);
        verify(aiEmbeddedBookRepository, times(1)).findByBook_IdIn(bookIds);
    }

    @Test
    public void testGetAiEmbeddedBooksReturnsEmptyListWhenNoneFound() {
        List<Integer> bookIds = List.of(1, 2, 3);
        when(aiEmbeddedBookRepository.findByBook_IdIn(bookIds)).thenReturn(List.of());

        List<AiEmbeddedBook> result = aiEmbeddedBookService.getAiEmbeddedBooks(bookIds);

        assertTrue(result.isEmpty());
    }
}
