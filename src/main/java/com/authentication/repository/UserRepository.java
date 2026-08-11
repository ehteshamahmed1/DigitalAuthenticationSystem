package com.authentication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.authentication.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find User by Email
    Optional<User> findByEmail(String email);

    // Find User by Mobile
    Optional<User> findByMobile(String mobile);

    // Find User by Email OR Mobile (allows login using either)
    Optional<User> findByEmailOrMobile(String email, String mobile);

    // Check Email Exists
    boolean existsByEmail(String email);

    // Check Mobile Exists
    boolean existsByMobile(String mobile);

}