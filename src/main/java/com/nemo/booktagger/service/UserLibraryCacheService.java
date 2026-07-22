package com.nemo.booktagger.service;

import com.nemo.booktagger.rest.dto.response.common.UserLibraryCache;

public interface UserLibraryCacheService {

    UserLibraryCache getUserLibrary(Integer userId);

    void invalidate(Integer userId);
}
