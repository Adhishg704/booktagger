package com.nemo.booktagger.service;

import com.nemo.booktagger.rest.dto.response.tags.TagDashboardResponse;
import com.nemo.booktagger.rest.dto.response.years.YearlyDashboardResponse;

import java.util.List;

public interface UserLibraryService {

    YearlyDashboardResponse getYearDashboardForUserLibrary(Integer userId);

    List<TagDashboardResponse> getTagDashboardForUserLibrary(Integer userId);
}
