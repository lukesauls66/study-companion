package com.lukesauls.studycompanion.studycompanion_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lukesauls.studycompanion.studycompanion_backend.dto.UserDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.InvalidPasswordChangeException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UserAlreadyExistsException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UserNotFoundException;
import com.lukesauls.studycompanion.studycompanion_backend.model.Role;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.User;
import com.lukesauls.studycompanion.studycompanion_backend.repository.postgres.UserRepository;
import org.springframework.lang.NonNull;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Create new user if user doesn't already exist
     */
    // FIXME: Add password encoding when adding auth
    public User createUser(@NonNull UserDto.Create userDto) {
        if (userRepository.findByEmail(userDto.email()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        User user = new User(userDto.email(), userDto.name(), userDto.username(), userDto.password());

        return userRepository.save(user);
    }

    /**
     * Get user by ID if user exists
     */
    @SuppressWarnings("null")
    public @NonNull User getUserById(@NonNull UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }

        return userRepository.findById(id).get();
    }

    /**
     * Get user by email if user exists
     */
    public User getUserByEmail(@NonNull String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User with that email not found");
        }

        return userRepository.findByEmail(email).get();
    }

    /**
     * Get user by username if user exists
     */
    public User getUserByUsername(@NonNull String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new UserNotFoundException("User with that username not found");
        }

        return userRepository.findByUsername(username).get();
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get all users of a certain role
     */
    public List<User> getAllUsersOfARole(@NonNull Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Get all users verified/unverified
     */
    public List<User> getAllVerifiedOrUnverifiedUsers(boolean isVerified) {
        return userRepository.findByIsVerified(isVerified);
    }

    /**
     * Get all users who haven't logged in before a certain date
     */
    public List<User> getInactiveUsersSince(@NonNull LocalDateTime date) {
        return userRepository.findByLastLoginBefore(date);
    }

    /**
     * Update user excluding password
     */
    public User updateUser(@NonNull UUID userId, @NonNull UserDto.Update userDto) {
        User existingUser = getUserById(userId);

        if (userDto.email() != null && !userDto.email().equals(existingUser.getEmail())) {
            if (userRepository.findByEmail(userDto.email()).isPresent()) {
                throw new UserAlreadyExistsException("User with this email already exists");
            }

            existingUser.setEmail(userDto.email());
        }

        if (userDto.name() != null) {
            existingUser.setName(userDto.name());
        }

        if (userDto.username() != null) {
            existingUser.setUsername(userDto.username());
        }

        return userRepository.save(existingUser);
    }

    /**
     * Update user password
     */
    // FIXME: Add password encoding when adding auth
    public User updateUserPassword(@NonNull UUID userId, @NonNull UserDto.ChangePassword userDto) {
        User existingUser = getUserById(userId);

        if (!userDto.currPassword().equals(existingUser.getPassword())) {
            throw new InvalidPasswordChangeException("Current password is incorrect");
        } else if (userDto.newPassword().equals(userDto.currPassword())) {
            throw new InvalidPasswordChangeException("Cannot change password to existing password");
        } else if (!userDto.newPassword().equals(userDto.confirmNewPassword())) {
            throw new InvalidPasswordChangeException("New password doesn't match confirm password");
        }

        existingUser.setPassword(userDto.newPassword());

        return userRepository.save(existingUser);
    }

    /**
     * update user's last login
     */
    public void updateLastLogin(@NonNull UUID userId) {
        User existingUser = getUserById(userId);
        existingUser.setLastLogin();

        userRepository.save(existingUser);
    }

    /**
     * Delete user
     */
    public void deleteUser(@NonNull UUID userId) {
        getUserById(userId);

        userRepository.deleteById(userId);
    }

    /**
     * Verify user
     */
    public User verifyUser(@NonNull UUID userId) {
        User existingUser = getUserById(userId);
        existingUser.setVerified(true);

        return userRepository.save(existingUser);
    }
}
