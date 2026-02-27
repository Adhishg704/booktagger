package com.nemo.booktagger.rest.dto.response.years;

import com.nemo.booktagger.rest.dto.response.common.UserBookDetailedResponse;

import java.util.List;
import java.util.Map;

public record YearlyDashboardResponse(
        Map<String, List<UserBookDetailedResponse>> byYearRead,
        Map<String, List<UserBookDetailedResponse>> byYearPublished
) {
}
