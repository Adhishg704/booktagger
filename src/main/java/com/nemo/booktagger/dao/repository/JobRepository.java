package com.nemo.booktagger.dao.repository;

import com.nemo.booktagger.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Integer> {
}
