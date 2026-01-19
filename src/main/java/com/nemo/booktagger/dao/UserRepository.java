package com.nemo.booktagger.dao;

import com.nemo.booktagger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<String> findUsernameById(Integer userId);
    Optional<String> findEmailById(Integer userId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
