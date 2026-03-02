package com.nemo.booktagger.rest.controller;

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
}
