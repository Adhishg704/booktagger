package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.rest.dto.response.tags.TagDashboardResponse;
import com.nemo.booktagger.rest.dto.response.tags.TagNameGroup;
import com.nemo.booktagger.rest.dto.response.years.YearlyDashboardResponse;
import com.nemo.booktagger.service.UserLibraryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UserLibraryServiceImpl implements UserLibraryService {

    private final UserBookRepository userBookRepository;

    public UserLibraryServiceImpl(UserBookRepository userBookRepository) {
        this.userBookRepository = userBookRepository;
    }

    @Override
    public YearlyDashboardResponse getYearDashboardForUserLibrary(Integer userId) {
        List<Object[]> userLibrary = getUserLibrary(userId);

        Map<Integer, UserBookDetailedResponse> masterBookMap = new HashMap<>();
        Map<String, List<UserBookDetailedResponse>> yearReadMap = new TreeMap<>();
        Map<String, List<UserBookDetailedResponse>> yearPublishedMap = new TreeMap<>();

        for(Object[] bookTagData: userLibrary) {
            UserBook userBook = (UserBook) bookTagData[0];
            BookTag bookTag = (BookTag) bookTagData[1];
            Book book = userBook.getBook();
            Tag tag = bookTag.getTag();

            UserBookDetailedResponse response = masterBookMap.get(book.getId());

            if(response == null) {
                response = new UserBookDetailedResponse(
                        book.getTitle(),
                        book.getAuthor(),
                        book.getYearPublished(),
                        userBook.getYearRead(),
                        book.getDescription(),
                        book.getThumbnailURL(),
                        userBook.getRating(),
                        new ArrayList<>()
                );

                String yr = userBook.getYearRead() != null ? userBook.getYearRead() : "Unknown";
                yearReadMap.computeIfAbsent(yr, k -> new ArrayList<>()).add(response);

                String yp = book.getYearPublished() != null ? book.getYearPublished() : "Unknown";
                yearPublishedMap.computeIfAbsent(yp, k -> new ArrayList<>()).add(response);
            }

            String tagName = tag.getTagName();
            if(!response.tags().contains(tagName)) {
                response.tags().add(tagName);
            }

            masterBookMap.put(book.getId(), response);
        }

        return new YearlyDashboardResponse(yearReadMap, yearPublishedMap);
    }


    @Override
    public List<TagDashboardResponse> getTagDashboardForUserLibrary(Integer userId) {
        List<Object[]> userLibrary = getUserLibrary(userId);

        Map<Integer, UserBookDetailedResponse> masterBookMap = new HashMap<>();
        Map<TagType, Map<String, List<UserBookDetailedResponse>>> booksByTagNameGroupedByTagType = new HashMap<>();

        for(Object[] bookTagData: userLibrary) {
            UserBook userBook = (UserBook) bookTagData[0];
            BookTag bookTag = (BookTag) bookTagData[1];
            Book book = userBook.getBook();
            Tag tag = bookTag.getTag();
            String tagName = tag.getTagName();

            UserBookDetailedResponse response = masterBookMap.computeIfAbsent(book.getId(), k -> new UserBookDetailedResponse(
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYearPublished(),
                    userBook.getYearRead(),
                    book.getDescription(),
                    book.getThumbnailURL(),
                    userBook.getRating(),
                    new ArrayList<>()
            ));

            if(!response.tags().contains(tagName)) {
                response.tags().add(tagName);
            }

            TagType tagType = tag.getTagType();
            booksByTagNameGroupedByTagType.computeIfAbsent(tagType, k -> new HashMap<>())
                    .computeIfAbsent(tagName, k -> new ArrayList<>()).add(response);
        }

        return booksByTagNameGroupedByTagType.entrySet().stream()
                .map(tagTypeMapEntry -> new TagDashboardResponse(
                        tagTypeMapEntry.getKey().name(),
                        tagTypeMapEntry.getValue().entrySet().stream()
                                .map(tagNameEntry -> new TagNameGroup(
                                        tagNameEntry.getKey(),
                                        tagNameEntry.getValue()
                                )).toList()
                )).toList();
    }

    private List<Object[]> getUserLibrary(Integer userId) {
        return userBookRepository.getAllUserLibraryData(userId);
    }
}
