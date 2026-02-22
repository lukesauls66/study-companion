package com.study_companion.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.study_companion.backend.dto.UserDto;
import com.study_companion.backend.exception.user.InvalidPasswordChangeException;
import com.study_companion.backend.exception.user.InvalidUserCreationException;
import com.study_companion.backend.exception.user.InvalidUserParameterException;
import com.study_companion.backend.exception.user.InvalidUserUpdateException;
import com.study_companion.backend.exception.user.UnauthorizedUserAccessException;
import com.study_companion.backend.exception.user.UserAlreadyExistsException;
import com.study_companion.backend.exception.user.UserException;
import com.study_companion.backend.exception.user.UserNotFoundException;
import com.study_companion.backend.exception.user.UserOperationException;
import com.study_companion.backend.model.Role;
import com.study_companion.backend.model.postgres.User;
import com.study_companion.backend.repository.postgres.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user if email doesn't already exist.
     * Validates all required fields are not empty.
     * Sets default role to USER and verified status to false.
     * 
     * @param userDto the user creation data containing email, name, username, and
     *                password
     * @return the created user with generated ID and timestamps
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws InvalidUserCreationException  if any required field is empty
     * @throws UserAlreadyExistsException    if a user with the email already exists
     * @throws UserOperationException        if server error occurs
     */  
    public UserDto.Get createUser(UserDto.Create userDto) {
        if (userDto == null) {
            throw new InvalidUserParameterException("User data cannot be null when creating a user");
        }

        if (userDto.email().trim().isEmpty()) {
            throw new InvalidUserCreationException("Email cannot be empty");
        }

        if (userDto.name().trim().isEmpty()) {
            throw new InvalidUserCreationException("Name cannot be empty");
        }

        if (userDto.username().trim().isEmpty()) {
            throw new InvalidUserCreationException("Username cannot be empty");
        }

        if (userDto.rawPassword().trim().isEmpty()) {
            throw new InvalidUserCreationException("Password cannot be empty");
        }

        try {
            logger.debug("Checking if user with email, {}, already exists", userDto.email());
            if (userRepository.existsByEmail(userDto.email())) {
                throw new UserAlreadyExistsException("User with this email already exists");
            }

            logger.debug("Creating user");
            User user = new User(userDto.email().trim(), userDto.name().trim(), userDto.username().trim(),
                    passwordEncoder.encode(userDto.rawPassword().trim()));

            User savedUser = userRepository.save(user);
            logger.info("Successfully created user");
            return convertToDto(savedUser);
        } catch (UserAlreadyExistsException e) {
            logger.error("User with this email already exists: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create user: {}", e.getMessage());
            throw new UserOperationException("Failed to create user", e);
        }
    }

    /**
     * Retrieves a user by their unique identifier.
     * 
     * @param id the UUID of the user to retrieve
     * @return the user with the specified ID
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws UserNotFoundException         if no user exists with the given ID
     * @throws UserOperationException        if server error occurs
     */
    public UserDto.Get getUserById(UUID id) {
        if (id == null) {
            throw new InvalidUserParameterException("ID cannot be null");
        }

        try {
            logger.debug("Checking if user exists by id: {}", id);
            if (!userRepository.existsById(id)) {
                throw new UserNotFoundException("User with ID " + id + " not found");
            }

            User foundUser = userRepository.findById(id).get();
            logger.info("User found with provided id");
            return convertToDto(foundUser);
        } catch (UserNotFoundException e) {
            logger.error("User not found with provided id: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch user: {}", e.getMessage());
            throw new UserOperationException("Failed to fetch user", e);
        }
    }

    /**
     * Retrieves a user by their email address.
     * 
     * @param email the email address of the user to retrieve
     * @return the user with the specified email
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws UserNotFoundException         if no user exists with the given email
     * @throws UserOperationException        if server error occurs
     */
    public UserDto.Get getUserByEmail(String email) {
        if (email == null) {
            throw new InvalidUserParameterException("Email cannot be null");
        }

        try {
            logger.debug("Checking if user exists with email: {}", email);
            if (!userRepository.existsByEmail(email)) {
                throw new UserNotFoundException("User with that email not found");
            }

            User foundUser = userRepository.findByEmail(email).get();
            logger.info("User found with provided email");
            return convertToDto(foundUser);
        } catch (UserNotFoundException e) {
            logger.error("User not found with provided email: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch user: {}", e.getMessage());
            throw new UserOperationException("Failed to fetch user", e);
        }
    }

    /**
     * Retrieves a user by their username.
     * 
     * @param username the username of the user to retrieve
     * @return the user with the specified username
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws UserNotFoundException         if no user exists with the given
     *                                       username
     * @throws UserOperationException        if server error occurs
     */
    public UserDto.Get getUserByUsername(String username) {
        if (username == null) {
            throw new InvalidUserParameterException("Username cannot be null");
        }

        try {
            logger.debug("Checking if user exists with username: {}", username);
            if (!userRepository.existsByUsername(username)) {
                throw new UserNotFoundException("User with that username not found");
            }

            User foundUser = userRepository.findByUsername(username).get();
            logger.info("User found with provided username");
            return convertToDto(foundUser);
        } catch (UserNotFoundException e) {
            logger.error("User not found with provided username: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch user: {}", e.getMessage());
            throw new UserOperationException("Failed to fetch user", e);
        }
    }

    /**
     * Retrieves all users in the system.
     * Should typically be restricted to admin users in production.
     * 
     * @return a list of all users in the system
     * @throws UnauthorizedUserAccessException if authentication fails or user is not an admin
     * @throws UserOperationException if server error occurs
     */
    public List<UserDto.Get> getAllUsers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedUserAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin");
            if (!isAdmin) {
                throw new UnauthorizedUserAccessException("Unauthorized user access");
            }
                    
            logger.debug("Searching for all users");
            List<UserDto.Get> users = userRepository.findAll().stream().map(this::convertToDto).toList();
            logger.info("Found all users");
            return users;
        } catch (UnauthorizedUserAccessException e) {
            logger.error("UnauthorizedUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch all users: {}", e.getMessage());
            throw new UserOperationException("Failed to fetch users", e);
        }
    }

    /**
     * Retrieves all users with a specific role.
     * Useful for admin operations and role-based filtering.
     * 
     * @param role the role to filter users by
     * @return a list of users with the specified role
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws UnauthorizedUserAccessException if authentication fails or user is not an admin
     * @throws UserOperationException        if server error occurs
     */
    public List<UserDto.Get> getAllUsersOfARole(Role role) {
        if (role == null) {
            throw new InvalidUserParameterException("Roll cannot be null");
        }

        try { 
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedUserAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin");
            if (!isAdmin) {
                throw new UnauthorizedUserAccessException("Unauthorized user access");
            }

            logger.debug("Searching for users with role: {}", role);
            List<UserDto.Get> users = userRepository.findByRole(role).stream().map(this::convertToDto).toList(); 
            logger.info("Found users with provided role");
            return users; 
        } catch (UnauthorizedUserAccessException e) {
            logger.error("UnauthorizedUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch users of provided role {}: {}", role, e.getMessage());
            throw new UserOperationException("Failed to fetch users of " + role + " role", e);
        }
    }

    /**
     * Marks a user as verified.
     * Typically used after email verification process.
     * 
     * @param userId the UUID of the user to verify
     * @return the updated user with verified status set to true
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws UserNotFoundException         if no user exists with the given ID
     * @throws UserOperationException        if server error occurs
     */
    public UserDto.Get verifyUser(UUID userId) {
        if (userId == null) {
            throw new InvalidUserParameterException("userId cannot be null");
        }

        try {
            logger.debug("Searching for user with provided id");
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
            logger.debug("Found user, now verifying user");
            existingUser.setVerified(true);

            User updatedUser = userRepository.save(existingUser);
            logger.info("Successfully verified user");
            return convertToDto(updatedUser);
        } catch (UserNotFoundException e) {
            logger.error("User not found with provided id: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to verify user: {}", e.getMessage());
            throw new UserOperationException("Failed to verify user", e);
        }
    }

    /**
     * Retrieves all users filtered by their verification status.
     * Useful for admin operations and user management.
     * 
     * @param isVerified true to get verified users, false to get unverified users
     * @return a list of users with the specified verification status
     * @throws UserOperationException if server error occurs
     */
    public List<UserDto.Get> getAllVerifiedOrUnverifiedUsers(boolean isVerified) {
        try {
            logger.debug("Searching for users by verified status: {}", isVerified);
            List<UserDto.Get> users = userRepository.findByIsVerified(isVerified).stream().map(this::convertToDto).toList();
            logger.info("Found all users with verified status: {}", isVerified);
            return users; 
        } catch (Exception e) {
            logger.error("Failed to fetch all users with provided verified status {}: {}", isVerified, e.getMessage());
            throw new UserOperationException("Failed to fetch all users with provided verified status", e);
        }
    }

    /**
     * Updates a user's last login timestamp to the current time.
     * Called during authentication process to track user activity.
     * 
     * @param userId the UUID of the user whose last login to update
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws UserNotFoundException         if no user exists with the given ID
     * @throws UserOperationException        if server error occurs
     */
    public void updateLastLogin(UUID userId) {
        if (userId == null) {
            throw new InvalidUserParameterException("userId cannot be null");
        }

        try {
            logger.debug("Searching for user with provided id");
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
            logger.debug("Found user, setting user's last login");
            existingUser.setLastLogin();

            userRepository.save(existingUser);
            logger.info("Successfully updated user's last login");
        } catch (UserNotFoundException e) {
            logger.error("User not found with provided id: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update user's last login: {}", e.getMessage());
            throw new UserOperationException("Failed to update user's last login", e);
        }
    }

    /**
     * Retrieves all users who haven't logged in since a specified date.
     * Useful for identifying inactive users for cleanup or re-engagement.
     * 
     * @param date the cutoff date - users with last login before this date are
     *             considered inactive
     * @return a list of users who haven't logged in since the specified date
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws UnauthorizedUserAccessException if authentication fails or user is not an admin
     * @throws UserOperationException        if server error occurs
     */
    public List<UserDto.Get> getInactiveUsersSince(LocalDateTime date) {
        if (date == null) {
            throw new InvalidUserParameterException("Date cannot be null");
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedUserAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin");
            if (!isAdmin) {
                throw new UnauthorizedUserAccessException("Unauthorized user access");
            }

            logger.debug("Searching for users that have not logged in since {}", date);
            List<UserDto.Get> users = userRepository.findByLastLoginBefore(date).stream().map(this::convertToDto).toList();
            logger.info("Successfully fetched all users that have not logged in since {}", date);
            return users;
        } catch (UnauthorizedUserAccessException e) {
            logger.error("UnauthorizedUser: {}", e.getMessage());
            throw e;
        } catch (Exception e) { 
            logger.error("Failed to fetch users that have not logged in since {}: {}", date, e.getMessage());
            throw new UserOperationException("Failed to get users that have not logged in since " + date, e);
        }
    }

    /**
     * Updates user information excluding password.
     * At least one field must be provided for update.
     * Validates email uniqueness and prevents setting duplicate values.
     * 
     * @param userId  the UUID of the user to update
     * @param userDto the update data containing new email, name, and/or username
     * @return the updated user
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws UserNotFoundException         if no user exists with the given ID
     * @throws InvalidUserUpdateException    if no fields provided or email
     *                                       unchanged
     * @throws UserAlreadyExistsException    if email is already in use by another
     *                                       user
     * @throws UserOperationException        if server error occurs
     */
    public UserDto.Get updateUser(UUID userId, UserDto.Update userDto) {
        if (userId == null) {
            throw new InvalidUserParameterException("userId cannot be null");
        }

        if (userDto == null) {
            throw new InvalidUserParameterException("User data transfer object cannot be null");
        }

        boolean emailProvided = userDto.email() != null && !userDto.email().trim().isEmpty();
        boolean nameProvided = userDto.name() != null && !userDto.name().trim().isEmpty();
        boolean usernameProvided = userDto.username() != null && !userDto.username().trim().isEmpty();

        if (!emailProvided && !nameProvided && !usernameProvided) {
            throw new InvalidUserUpdateException("At least one field must be provided");
        }

        try {
            logger.debug("Searching for user by provided id");
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
            logger.debug("Found user by provided id");

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

                logger.debug("Updating user's email if provided");
                existingUser.setEmail(userDto.email().trim());
            }

            logger.debug("Updating user's name if provided");
            if (userDto.name() != null) {
                existingUser.setName(userDto.name().trim());
            }

            logger.debug("Updating user's username if provided");
            if (userDto.username() != null) {
                existingUser.setUsername(userDto.username().trim());
            }

            User updatedUser = userRepository.save(existingUser);
            logger.info("Successfully updated user");
            return convertToDto(updatedUser);
        } catch (UserException e) {
            logger.error("User update failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update user: {}", e.getMessage());
            throw new UserOperationException("Failed to update user", e);
        }
    }

    /**
     * Updates a user's password with proper validation.
     * Validates current password, ensures new password is different, and confirms
     * password match.
     * 
     * @param userId  the UUID of the user whose password to update
     * @param userDto the password change data containing current, new, and
     *                confirmation passwords
     * @return the updated user
     * @throws InvalidUserParameterException  if any nonnull arg is null
     * @throws UserNotFoundException          if no user exists with the given ID
     * @throws InvalidPasswordChangeException if current password is wrong, new
     *                                        password same as current, or passwords
     *                                        don't match
     * @throws UserOperationException         if server error occurs
     */
    public UserDto.Get updateUserPassword(UUID userId, UserDto.ChangePassword userDto) {
        if (userId == null) {
            throw new InvalidUserParameterException("userId cannot be null");
        }

        if (userDto == null) {
            throw new InvalidUserParameterException("User data transfer object cannot be null");
        }

        try {
            logger.debug("Searching for user by provided id");
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
            logger.debug("Found user with provided id");

            if (!passwordEncoder.matches(userDto.currPassword(), existingUser.getPassword())) {
                throw new InvalidPasswordChangeException("Current password is incorrect");
            } else if (passwordEncoder.matches(userDto.newRawPassword(), existingUser.getPassword())) {
                throw new InvalidPasswordChangeException("Cannot change password to existing password");
            } else if (!userDto.newRawPassword().equals(userDto.confirmNewRawPassword())) {
                throw new InvalidPasswordChangeException("New password doesn't match confirm password");
            }

            logger.debug("Updated user's password");
            existingUser.setPassword(passwordEncoder.encode(userDto.newRawPassword().trim()));

            User updatedUser = userRepository.save(existingUser);
            logger.info("Successfully updated user's password");
            return convertToDto(updatedUser);
        } catch (UserException e) {
            logger.error("User password change failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update user password: {}", e.getMessage());
            throw new UserOperationException("Failed to update user password", e);
        }
    }

    /**
     * Deletes a user from the system.
     * This will cascade delete all associated data (decks, uploads, etc.) due to
     * JPA cascade settings.
     * 
     * @param userId the UUID of the user to delete
     * @throws InvalidUserParameterException if any nonnull arg is null
     * @throws UserNotFoundException         if no user exists with the given ID
     * @throws UserOperationException        if server error occurs
     */
    public void deleteUser(UUID userId) {
        if (userId == null) {
            throw new InvalidUserParameterException("userId cannot be null");
        }

        try {
            logger.debug("Checking if user exists with provided id");
            getUserById(userId);

            userRepository.deleteById(userId);
            logger.info("Successfully deleted user with provided id");
        } catch (UserNotFoundException e) {
            logger.error("User not found with provided id: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete user: {}", e.getMessage());
            throw new UserOperationException("Failed to delete user", e);
        }
    }

    private UserDto.Get convertToDto(User user) {
        return new UserDto.Get(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getUsername(),
                user.getRole(),
                user.isVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
