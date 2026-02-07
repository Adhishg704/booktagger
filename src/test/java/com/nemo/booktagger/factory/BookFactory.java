package com.nemo.booktagger.factory;

import com.nemo.booktagger.entity.Book;

import java.util.ArrayList;
import java.util.List;

public final class BookFactory {
    private BookFactory() {

    }

    private static int counter = 0;

    public static Book createBook(String title, String author, String description, long isbn, String year) {
        return new Book(title, author, description, isbn, year);
    }

    private static Book createBook() {
        counter++;
        return createBook("Book " + counter, "Author " + counter, "Description " + counter,
                1000000000L + counter, "2025");
    }

    public static List<Book> createBooks(int count) {
        List<Book> books = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            books.add(createBook());
        }

        return books;
    }
}
