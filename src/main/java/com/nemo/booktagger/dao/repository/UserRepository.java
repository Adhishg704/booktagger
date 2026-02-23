package com.nemo.booktagger.dao.repository;

import com.nemo.booktagger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u.username from User u where u.id=:userId")
    Optional<String> findUsernameById(@Param("userId") Integer userId);

    @Query("select u.email from User u where u.id=:userId")
    Optional<String> findEmailById(@Param("userId") Integer userId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
