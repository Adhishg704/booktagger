package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.BookTagRepository;
import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.entity.Book;
import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.enums.YearType;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.rest.dto.response.tags.TagDashboardResponse;
import com.nemo.booktagger.rest.dto.response.tags.TagNameGroup;
import com.nemo.booktagger.rest.dto.response.years.YearlyDashboardResponse;
import com.nemo.booktagger.service.UserLibraryCacheService;
import com.nemo.booktagger.service.UserLibraryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class UserLibraryServiceImpl implements UserLibraryService {

    private final UserLibraryCacheService userLibraryCacheService;
    private final UserBookRepository userBookRepository;
    private final BookTagRepository bookTagRepository;

    public UserLibraryServiceImpl(UserLibraryCacheService userLibraryCacheService, UserBookRepository userBookRepository,
                                  BookTagRepository bookTagRepository) {
        this.userLibraryCacheService = userLibraryCacheService;
        this.userBookRepository = userBookRepository;
        this.bookTagRepository = bookTagRepository;
    }

    @Override
    public YearlyDashboardResponse getYearDashboardForUserLibrary(Integer userId) {
        List<Object[]> userLibrary = userLibraryCacheService.getUserLibrary(userId);

        Map<Integer, UserBookDetailedResponse> masterBookMap = new HashMap<>();
        Map<String, List<UserBookDetailedResponse>> yearReadMap = new TreeMap<>();
        Map<String, List<UserBookDetailedResponse>> yearPublishedMap = new TreeMap<>();

        for (Object[] bookTagData : userLibrary) {
            UserBook userBook = (UserBook) bookTagData[0];
            BookTag bookTag = (BookTag) bookTagData[1];
            Book book = userBook.getBook();

            UserBookDetailedResponse response = masterBookMap.get(book.getId());

            if (response == null) {
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

            if (bookTag != null && bookTag.getTag() != null) {
                String tagName = bookTag.getTag().getTagName();
                if (!response.tags().contains(tagName)) {
                    response.tags().add(tagName);
                }
            }

            masterBookMap.put(book.getId(), response);
        }

        return new YearlyDashboardResponse(yearReadMap, yearPublishedMap);
    }


    @Override
    public List<TagDashboardResponse> getTagDashboardForUserLibrary(Integer userId) {
        List<Object[]> userLibrary = userLibraryCacheService.getUserLibrary(userId);

        Map<Integer, UserBookDetailedResponse> masterBookMap = new HashMap<>();
        Map<TagType, Map<String, List<UserBookDetailedResponse>>> booksByTagNameGroupedByTagType = new HashMap<>();

        for (Object[] bookTagData : userLibrary) {
            UserBook userBook = (UserBook) bookTagData[0];
            BookTag bookTag = (BookTag) bookTagData[1];
            Book book = userBook.getBook();

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

            if (bookTag == null || bookTag.getTag() == null) {
                continue;
            }

            Tag tag = bookTag.getTag();
            String tagName = tag.getTagName();

            if (!response.tags().contains(tagName)) {
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

    @Override
    public List<String> getYearsPublishedForUser(Integer userId) {
        return userBookRepository.findDistinctYearPublished(userId, ReadingStatus.READ);
    }

    @Override
    public List<String> getYearsReadForUser(Integer userId) {
        return userBookRepository.findDistinctYearRead(userId);
    }

    @Override
    public List<String> getTagTypesForUser(Integer userId) {
        return bookTagRepository.getDistinctTagTypes(userId)
                .stream().map(Enum::name).toList();
    }

    @Override
    public List<String> getTagNamesForUserForTagType(Integer userId, String tagType) {
        List<TagDashboardResponse> tagDashboardForUserLibrary = getTagDashboardForUserLibrary(userId);

        List<String> tagNames = new ArrayList<>();

        tagDashboardForUserLibrary.stream()
                .filter(tagTypeGroup -> tagTypeGroup.tagType().equals(tagType))
                .map(TagDashboardResponse::booksByTag)
                .forEach(tagNameGroupList -> tagNameGroupList.forEach(
                        tagNameGroup -> tagNames.add(tagNameGroup.tagName())
                ));

        return tagNames;
    }

    @Override
    public List<UserBookDetailedResponse> getUserBooksByYear(Integer userId, YearType yearType, String year) {
        YearlyDashboardResponse yearDashboardForUserLibrary = getYearDashboardForUserLibrary(userId);

        return switch (yearType) {
            case READ -> yearDashboardForUserLibrary.byYearRead().getOrDefault(year, List.of());
            case PUBLISHED -> yearDashboardForUserLibrary.byYearPublished().getOrDefault(year, List.of());
        };
    }

    @Override
    public List<UserBookDetailedResponse> getUserBooksByTagName(Integer userId, String tagName) {
        List<TagDashboardResponse> tagDashboardForUserLibrary = getTagDashboardForUserLibrary(userId);

        return tagDashboardForUserLibrary.stream()
                .flatMap(tagTypeGroup -> tagTypeGroup.booksByTag().stream())
                .filter(tagNameGroup -> tagNameGroup.tagName().equals(tagName))
                .flatMap(tagNameGroup -> tagNameGroup.books().stream())
                .toList();
    }
}
