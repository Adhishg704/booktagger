package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookTagRepository extends JpaRepository<BookTag, Integer> {
}
