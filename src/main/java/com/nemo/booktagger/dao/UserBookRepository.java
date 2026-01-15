package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Integer> {
}
