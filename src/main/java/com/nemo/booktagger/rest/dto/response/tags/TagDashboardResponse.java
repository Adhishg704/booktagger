package com.nemo.booktagger.rest.dto.response.tags;

import java.util.List;

public record TagDashboardResponse(
        String tagType,
        List<TagNameGroup> booksByTag
) {
}
