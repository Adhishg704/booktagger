package com.nemo.booktagger.factory;

import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.entity.UserBook;

import java.util.ArrayList;
import java.util.List;

public final class BookTagFactory {

    private BookTagFactory() {

    }

    public static BookTag createBookTag(User user, Book book, Tag tag) {
        return new BookTag(
                user,
                book,
                tag
        );
    }

    public static List<BookTag> createBookTagList(List<UserBook> userBooks, List<Tag> tags) {
        List<BookTag> bookTagList = new ArrayList<>();

        for(UserBook userBook: userBooks) {
            for(Tag tag: tags) {
                bookTagList.add(createBookTag(userBook.getUser(), userBook.getBook(), tag));
            }
        }

        return bookTagList;
    }
}
