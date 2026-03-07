package com.nemo.booktagger.rest.controller;

import com.nemo.booktagger.enums.YearType;
import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;
import com.nemo.booktagger.rest.dto.response.tags.TagDashboardResponse;
import com.nemo.booktagger.rest.dto.response.years.YearlyDashboardResponse;
import com.nemo.booktagger.service.UserLibraryService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/library")
public class UserLibraryController {

    private final UserLibraryService userLibraryService;

    public UserLibraryController(UserLibraryService userLibraryService) {
        this.userLibraryService = userLibraryService;
    }

    @GetMapping("/{userId}/years")
    public YearlyDashboardResponse getYearlyDashboardData(
            @Parameter(description = "User ID", required = true)
            @PathVariable("userId") Integer userId
    ) {
        return userLibraryService.getYearDashboardForUserLibrary(userId);
    }

    @GetMapping("/{userId}/tags")
    public List<TagDashboardResponse> getTagDashboardData(
            @Parameter(description = "User ID", required = true)
            @PathVariable("userId") Integer userId
    ) {
        return userLibraryService.getTagDashboardForUserLibrary(userId);
    }

    @GetMapping("/{userId}/years-read")
    public List<String> getYearsRead(
            @PathVariable("userId") Integer userId
    ) {
        return userLibraryService.getYearsReadForUser(userId);
    }

    @GetMapping("/{userId}/years-published")
    public List<String> getYearsPublished(
            @PathVariable("userId") Integer userId
    ) {
        return userLibraryService.getYearsPublishedForUser(userId);
    }

    @GetMapping("/{userId}/books/year/{yearType}/{year}")
    public List<UserBookDetailedResponse> getUserBooksByYear(
            @PathVariable("userId") Integer userId,
            @PathVariable("yearType") YearType yearType,
            @PathVariable("year") String year
    ) {
        return userLibraryService.getUserBooksByYear(userId, yearType, year);
    }

    @GetMapping("/{userId}/tag-types")
    public List<String> getTagTypes(
            @PathVariable("userId") Integer userId
    ) {
        return userLibraryService.getTagTypesForUser(userId);
    }

    @GetMapping("/{userId}/tag-names/{tagType}")
    public List<String> getTagNamesForType(
            @PathVariable("userId") Integer userId,
            @PathVariable("tagType") String tagType
    ) {
        return userLibraryService.getTagNamesForUserForTagType(userId, tagType);
    }

    @GetMapping("/{userId}/books/tag/{tagName}")
    public List<UserBookDetailedResponse> getUserBooksByTagName(
            @PathVariable("userId") Integer userId,
            @PathVariable("tagName") String tagName
    ) {
        return userLibraryService.getUserBooksByTagName(userId, tagName);
    }
}
