package com.nemo.booktagger.service;

import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.enums.YearType;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;

import java.util.List;

public interface UserLibraryService {

    List<String> getYearsPublishedForUser(Integer userId);

    List<String> getYearsReadForUser(Integer userId);

    List<String> getTagTypesForUser(Integer userId);

    List<String> getTagNamesForUserForTagType(Integer userId, TagType tagType);

    List<UserBookDetailedResponse> getUserBooksByYear(Integer userId, YearType yearType, String year);

    List<UserBookDetailedResponse> getUserBooksByTagName(Integer userId, String tagName);
}
