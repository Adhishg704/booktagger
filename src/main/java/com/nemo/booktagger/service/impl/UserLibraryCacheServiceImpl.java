package com.nemo.booktagger.service.impl;

import com.nemo.booktagger.dao.repository.UserBookRepository;
import com.nemo.booktagger.enums.ReadingStatus;
import com.nemo.booktagger.rest.dto.response.common.UserLibraryCache;
import com.nemo.booktagger.service.UserLibraryCacheService;
import com.nemo.booktagger.service.mapper.UserLibraryMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserLibraryCacheServiceImpl implements UserLibraryCacheService {

    private final UserBookRepository userBookRepository;
    private final UserLibraryMapper userLibraryMapper;

    public UserLibraryCacheServiceImpl(UserBookRepository userBookRepository, UserLibraryMapper userLibraryMapper) {
        this.userBookRepository = userBookRepository;
        this.userLibraryMapper = userLibraryMapper;
    }

    @Override
    @Cacheable(value = "userLibrary", key = "#userId")
    public UserLibraryCache getUserLibrary(Integer userId) {
        List<Object[]> userBooksAndBookTags = userBookRepository.getAllUserLibraryData(userId, ReadingStatus.READ);
        return new UserLibraryCache(
                userLibraryMapper.toDetailedResponse(userBooksAndBookTags)
        );
    }

    @Override
    @CacheEvict(value = "userLibrary", key = "#userId")
    public void invalidate(Integer userId) {

    }
}
