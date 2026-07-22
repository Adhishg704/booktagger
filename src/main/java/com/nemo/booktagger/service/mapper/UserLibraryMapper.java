package com.nemo.booktagger.service.mapper;

import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserLibraryMapper {

    public List<UserBookDetailedResponse> toDetailedResponse(List<Object[]> userBooksAndBookTags) {
        Map<Integer, UserBookDetailedResponse> bookIdToDetailedResponseMap = new HashMap<>();

        for(Object[] userBookAndBookTag: userBooksAndBookTags) {
            UserBook userBook = (UserBook) userBookAndBookTag[0];
            BookTag bookTag = (BookTag) userBookAndBookTag[1];
            Book book = userBook.getBook();

            UserBookDetailedResponse response = bookIdToDetailedResponseMap.computeIfAbsent(book.getId(),
                    k -> new UserBookDetailedResponse(
                            book.getId(),
                            book.getTitle(),
                            book.getAuthor(),
                            book.getYearPublished(),
                            userBook.getYearRead(),
                            book.getDescription(),
                            book.getThumbnailURL(),
                            userBook.getRating(),
                            new ArrayList<>()
                    ));

            if(bookTag == null || bookTag.getTag() == null) {
                continue;
            }

            Tag tag = bookTag.getTag();
            String tagName = tag.getTagName();

            if(!response.tags().contains(tagName)) {
                response.tags().add(tagName);
            }
        }

        return bookIdToDetailedResponseMap.values().stream().toList();
    }
}
