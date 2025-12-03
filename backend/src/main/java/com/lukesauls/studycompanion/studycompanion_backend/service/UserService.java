package com.lukesauls.studycompanion.studycompanion_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lukesauls.studycompanion.studycompanion_backend.dto.UserDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.InvalidPasswordChangeException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.InvalidUserCreationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.InvalidUserUpdateException;
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
     * Creates a new user if email doesn't already exist.
     * Validates all required fields are not empty.
     * Sets default role to USER and verified status to false.
     * 
     * @param userDto the user creation data containing email, name, username, and password
     * @return the created user with generated ID and timestamps
     * @throws InvalidUserCreationException if any required field is empty
     * @throws UserAlreadyExistsException if a user with the email already exists
     */
    // FIXME: Add password encoding when adding auth
    public User createUser(@NonNull UserDto.Create userDto) {
        if (userDto.email().trim().isEmpty()) {
            throw new InvalidUserCreationException("Email cannot be empty");
        }

        if (userDto.name().trim().isEmpty()) {
            throw new InvalidUserCreationException("Name cannot be empty");
        }

        if (userDto.username().trim().isEmpty()) {
            throw new InvalidUserCreationException("Username cannot be empty");
        }

        if (userDto.password().trim().isEmpty()) {
            throw new InvalidUserCreationException("Password cannot be empty");
        }

        if (userRepository.findByEmail(userDto.email()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        User user = new User(userDto.email(), userDto.name(), userDto.username(), userDto.password());

        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their unique identifier.
     * 
     * @param id the UUID of the user to retrieve
     * @return the user with the specified ID
     * @throws UserNotFoundException if no user exists with the given ID
     */
    @SuppressWarnings("null")
    public @NonNull User getUserById(@NonNull UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }

        return userRepository.findById(id).get();
    }

    /**
     * Retrieves a user by their email address.
     * 
     * @param email the email address of the user to retrieve
     * @return the user with the specified email
     * @throws UserNotFoundException if no user exists with the given email
     */
    public User getUserByEmail(@NonNull String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User with that email not found");
        }

        return userRepository.findByEmail(email).get();
    }

    /**
     * Retrieves a user by their username.
     * 
     * @param username the username of the user to retrieve
     * @return the user with the specified username
     * @throws UserNotFoundException if no user exists with the given username
     */
    public User getUserByUsername(@NonNull String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new UserNotFoundException("User with that username not found");
        }

        return userRepository.findByUsername(username).get();
    }

    /**
     * Retrieves all users in the system.
     * Should typically be restricted to admin users in production.
     * 
     * @return a list of all users in the system
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves all users with a specific role.
     * Useful for admin operations and role-based filtering.
     * 
     * @param role the role to filter users by
     * @return a list of users with the specified role
     */
    public List<User> getAllUsersOfARole(@NonNull Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Marks a user as verified.
     * Typically used after email verification process.
     * 
     * @param userId the UUID of the user to verify
     * @return the updated user with verified status set to true
     * @throws UserNotFoundException if no user exists with the given ID
     */
    public User verifyUser(@NonNull UUID userId) {
        User existingUser = getUserById(userId);
        existingUser.setVerified(true);

        return userRepository.save(existingUser);
    }

    /**
     * Retrieves all users filtered by their verification status.
     * Useful for admin operations and user management.
     * 
     * @param isVerified true to get verified users, false to get unverified users
     * @return a list of users with the specified verification status
     */
    public List<User> getAllVerifiedOrUnverifiedUsers(boolean isVerified) {
        return userRepository.findByIsVerified(isVerified);
    }

    /**
     * Updates a user's last login timestamp to the current time.
     * Called during authentication process to track user activity.
     * 
     * @param userId the UUID of the user whose last login to update
     * @throws UserNotFoundException if no user exists with the given ID
     */
    public void updateLastLogin(@NonNull UUID userId) {
        User existingUser = getUserById(userId);
        existingUser.setLastLogin();

        userRepository.save(existingUser);
    }

    /**
     * Retrieves all users who haven't logged in since a specified date.
     * Useful for identifying inactive users for cleanup or re-engagement.
     * 
     * @param date the cutoff date - users with last login before this date are considered inactive
     * @return a list of users who haven't logged in since the specified date
     */
    public List<User> getInactiveUsersSince(@NonNull LocalDateTime date) {
        return userRepository.findByLastLoginBefore(date);
    }

    /**
     * Updates user information excluding password.
     * At least one field must be provided for update.
     * Validates email uniqueness and prevents setting duplicate values.
     * 
     * @param userId the UUID of the user to update
     * @param userDto the update data containing new email, name, and/or username
     * @return the updated user
     * @throws UserNotFoundException if no user exists with the given ID
     * @throws InvalidUserUpdateException if no fields provided or email unchanged
     * @throws UserAlreadyExistsException if email is already in use by another user
     */
    public User updateUser(@NonNull UUID userId, @NonNull UserDto.Update userDto) {
        boolean emailProvided = userDto.email() != null && !userDto.email().trim().isEmpty();
        boolean nameProvided = userDto.name() != null && !userDto.name().trim().isEmpty();
        boolean usernameProvided = userDto.username() != null && !userDto.username().trim().isEmpty();

        if (!emailProvided && !nameProvided && !usernameProvided) {
            throw new InvalidUserUpdateException("At least one field must be provided");
        }

        User existingUser = getUserById(userId);

        if (userDto.email() != null) {
            if (userDto.email().equals(existingUser.getEmail())) {
                throw new InvalidUserUpdateException("Email is already set to this value");
            }
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
     * Updates a user's password with proper validation.
     * Validates current password, ensures new password is different, and confirms password match.
     * 
     * @param userId the UUID of the user whose password to update
     * @param userDto the password change data containing current, new, and confirmation passwords
     * @return the updated user
     * @throws UserNotFoundException if no user exists with the given ID
     * @throws InvalidPasswordChangeException if current password is wrong, new password same as current, or passwords don't match
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
     * Deletes a user from the system.
     * This will cascade delete all associated data (decks, uploads, etc.) due to JPA cascade settings.
     * 
     * @param userId the UUID of the user to delete
     * @throws UserNotFoundException if no user exists with the given ID
     */
    public void deleteUser(@NonNull UUID userId) {
        getUserById(userId);

        userRepository.deleteById(userId);
    }
}
