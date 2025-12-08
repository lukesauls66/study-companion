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
import com.lukesauls.studycompanion.studycompanion_backend.exception.UserOperationException;
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
        
        try {
            if (userRepository.existsByEmail(userDto.email())) {
                throw new UserAlreadyExistsException("User with this email already exists");
            }
        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new UserOperationException("Failed to check email uniqueness", e);
        }

        try {
            User user = new User(userDto.email().trim(), userDto.name().trim(), userDto.username().trim(), userDto.password().trim());
    
            return userRepository.save(user);
        } catch (Exception e) {
            throw new UserOperationException("Failed to create user", e);
        }
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
        try {
            if (!userRepository.existsById(id)) {
                throw new UserNotFoundException("User with ID " + id + " not found");
            }

            return userRepository.findById(id).get();
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserOperationException("Failed to fetch user", e);
        }
    }

    /**
     * Retrieves a user by their email address.
     * 
     * @param email the email address of the user to retrieve
     * @return the user with the specified email
     * @throws UserNotFoundException if no user exists with the given email
     */
    public User getUserByEmail(@NonNull String email) {
        try {
            if (!userRepository.existsByEmail(email)) {
                throw new UserNotFoundException("User with that email not found");
            }

            return userRepository.findByEmail(email).get();
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserOperationException("Failed to fetch user", e);
        }        
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

        try {
            return userRepository.findByUsername(username).get();
        } catch (Exception e) {
            throw new UserOperationException("Failed to fetch user", e);
        }
    }

    /**
     * Retrieves all users in the system.
     * Should typically be restricted to admin users in production.
     * 
     * @return a list of all users in the system
     */
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            throw new UserOperationException("Failed to fetch users", e);
        }
    }

    /**
     * Retrieves all users with a specific role.
     * Useful for admin operations and role-based filtering.
     * 
     * @param role the role to filter users by
     * @return a list of users with the specified role
     */
    public List<User> getAllUsersOfARole(@NonNull Role role) {
        try {
            return userRepository.findByRole(role);
        } catch (Exception e) {
            throw new UserOperationException("Failed to fetch users of " + role + " role", e);
        }
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
        try {
            User existingUser = getUserById(userId);
            existingUser.setVerified(true);
    
            return userRepository.save(existingUser);
        } catch (Exception e) {
            throw new UserOperationException("Failed to verify user", e);
        }
    }

    /**
     * Retrieves all users filtered by their verification status.
     * Useful for admin operations and user management.
     * 
     * @param isVerified true to get verified users, false to get unverified users
     * @return a list of users with the specified verification status
     */
    public List<User> getAllVerifiedOrUnverifiedUsers(boolean isVerified) {
        try {
            return userRepository.findByIsVerified(isVerified);
        } catch (Exception e) {
            throw new UserOperationException("Failed to fetch all verified users", e);
        }
    }

    /**
     * Updates a user's last login timestamp to the current time.
     * Called during authentication process to track user activity.
     * 
     * @param userId the UUID of the user whose last login to update
     * @throws UserNotFoundException if no user exists with the given ID
     */
    public void updateLastLogin(@NonNull UUID userId) {
        try {
            User existingUser = getUserById(userId);
            existingUser.setLastLogin();
    
            userRepository.save(existingUser);
        } catch (Exception e) {
            throw new UserOperationException("Failed to update user's last login", e);
        }
    }

    /**
     * Retrieves all users who haven't logged in since a specified date.
     * Useful for identifying inactive users for cleanup or re-engagement.
     * 
     * @param date the cutoff date - users with last login before this date are considered inactive
     * @return a list of users who haven't logged in since the specified date
     */
    public List<User> getInactiveUsersSince(@NonNull LocalDateTime date) {
        try {
            return userRepository.findByLastLoginBefore(date);
        } catch (Exception e) {
            throw new UserOperationException("Failed to get users that haven't logged in since " + date, e);
        }
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
                try {
                    if (userRepository.findByEmail(userDto.email()).isPresent()) {
                        throw new UserAlreadyExistsException("User with this email already exists");
                    }
                } catch (UserAlreadyExistsException e) {
                    throw e;
                } catch (Exception e) {
                    throw new UserOperationException("Failed to check email uniqueness", e);
                }
                
                existingUser.setEmail(userDto.email().trim());
            }
    
            if (userDto.name() != null) {
                existingUser.setName(userDto.name().trim());
            }
            
            if (userDto.username() != null) {
                existingUser.setUsername(userDto.username().trim());
            }
            
        try {
            return userRepository.save(existingUser);
        } catch (Exception e) {
            throw new UserOperationException("Failed to update user", e);
        }
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

        existingUser.setPassword(userDto.newPassword().trim());

        try {
            return userRepository.save(existingUser);
        } catch (Exception e) {
            throw new UserOperationException("Failed to update user password", e);
        }
    }

    /**
     * Deletes a user from the system.
     * This will cascade delete all associated data (decks, uploads, etc.) due to JPA cascade settings.
     * 
     * @param userId the UUID of the user to delete
     * @throws UserNotFoundException if no user exists with the given ID
     */
    public void deleteUser(@NonNull UUID userId) {
        try {
            getUserById(userId);
    
            userRepository.deleteById(userId);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserOperationException("Failed to delete user", e);
        }
    }
}
