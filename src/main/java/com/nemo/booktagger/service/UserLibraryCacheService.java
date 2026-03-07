package com.nemo.booktagger.service;

import java.util.List;

public interface UserLibraryCacheService {

    List<Object[]> getUserLibrary(Integer userId);
}
