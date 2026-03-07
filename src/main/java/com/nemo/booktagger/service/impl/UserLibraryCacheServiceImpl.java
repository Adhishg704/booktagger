package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.service.UserLibraryCacheService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserLibraryCacheServiceImpl implements UserLibraryCacheService {

    private final UserBookRepository userBookRepository;

    public UserLibraryCacheServiceImpl(UserBookRepository userBookRepository) {
        this.userBookRepository = userBookRepository;
    }

    @Override
    @Cacheable(value = "userLibrary", key = "#userId")
    public List<Object[]> getUserLibrary(Integer userId) {
        return userBookRepository.getAllUserLibraryData(userId, ReadingStatus.READ);
    }
}
