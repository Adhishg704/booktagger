package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {
}
