package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.BookTagRepository;
import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.enums.YearType;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.service.UserLibraryCacheService;
import com.nemo.booktagger.service.UserLibraryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
    public List<String> getTagNamesForUserForTagType(Integer userId, TagType tagType) {
        return bookTagRepository.getDistinctTagNamesByUserIdAndTagType(userId, tagType);
    }

    @Override
    public List<UserBookDetailedResponse> getUserBooksByYear(Integer userId, YearType yearType, String year) {
        List<UserBookDetailedResponse> userBookDetailedResponses = userLibraryCacheService.getUserLibrary(userId).books();

        return userBookDetailedResponses
                .stream()
                .filter(book -> switch (yearType) {
                    case READ -> Objects.equals(book.yearRead(), year);
                    case PUBLISHED -> Objects.equals(book.yearPublished(), year);
                })
                .toList();
    }

    @Override
    public List<UserBookDetailedResponse> getUserBooksByTagName(Integer userId, String tagName) {
        List<UserBookDetailedResponse> userBookDetailedResponses = userLibraryCacheService.getUserLibrary(userId).books();

        return userBookDetailedResponses
                .stream()
                .filter(userBook -> userBook.tags().contains(tagName))
                .toList();
    }
}
