package com.study_companion.backend.repository.postgres;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.study_companion.backend.model.Role;
import com.study_companion.backend.model.postgres.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if username already exists
     */
    boolean existsByUsername(String username);

    /**
     * Find users by role
     */
    List<User> findByRole(Role role);

    /**
     * Find verified/unverified users
     */
    List<User> findByIsVerified(boolean isVerified);

    /**
     * Find users who haven't logged in recently
     */
    List<User> findByLastLoginBefore(LocalDateTime cutoffDate);
}
