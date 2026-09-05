package com.nemo.booktagger.rest.controller;

import com.nemo.booktagger.enums.TagType;
import com.nemo.booktagger.enums.YearType;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.security.AuthenticatedUserService;
import com.nemo.booktagger.service.UserLibraryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/library")
public class UserLibraryController {

    private final AuthenticatedUserService authenticatedUserService;
    private final UserLibraryService userLibraryService;

    public UserLibraryController(AuthenticatedUserService authenticatedUserService, UserLibraryService userLibraryService) {
        this.authenticatedUserService = authenticatedUserService;
        this.userLibraryService = userLibraryService;
    }

    @GetMapping("/years-read")
    public List<String> getYearsRead() {
        Integer userId = authenticatedUserService.getAuthenticatedUserId();

        return userLibraryService.getYearsReadForUser(userId);
    }

    @GetMapping("/years-published")
    public List<String> getYearsPublished() {
        Integer userId = authenticatedUserService.getAuthenticatedUserId();

        return userLibraryService.getYearsPublishedForUser(userId);
    }

    @GetMapping("/books/year/{yearType}/{year}")
    public List<UserBookDetailedResponse> getUserBooksByYear(
            @PathVariable("yearType") YearType yearType,
            @PathVariable("year") String year
    ) {
        Integer userId = authenticatedUserService.getAuthenticatedUserId();

        return userLibraryService.getUserBooksByYear(userId, yearType, year);
    }

    @GetMapping("/tag-types")
    public List<String> getTagTypes() {
        Integer userId = authenticatedUserService.getAuthenticatedUserId();

        return userLibraryService.getTagTypesForUser(userId);
    }

    @GetMapping("/tag-names/{tagType}")
    public List<String> getTagNamesForType(
            @PathVariable("tagType") TagType tagType
    ) {
        Integer userId = authenticatedUserService.getAuthenticatedUserId();

        return userLibraryService.getTagNamesForUserForTagType(userId, tagType);
    }

    @GetMapping("/books/tag/{tagName}")
    public List<UserBookDetailedResponse> getUserBooksByTagName(
            @PathVariable("tagName") String tagName
    ) {
        Integer userId = authenticatedUserService.getAuthenticatedUserId();

        return userLibraryService.getUserBooksByTagName(userId, tagName);
    }
}
