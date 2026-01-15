package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {
}
