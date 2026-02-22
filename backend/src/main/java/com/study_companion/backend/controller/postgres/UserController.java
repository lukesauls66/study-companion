package com.study_companion.backend.controller.postgres;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study_companion.backend.dto.UserDto;
import com.study_companion.backend.exception.user.UnauthorizedUserAccessException;
import com.study_companion.backend.model.Role;
import com.study_companion.backend.service.UserService;
import com.study_companion.backend.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/user")
@CrossOrigin
@Tag(name = "User Management", description = "User administration and management operations")
public class UserController {

    private final UserService userService;

    private final SecurityUtils securityUtils;

    UserController(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/getUsers")
    @Operation(summary = "Get all users", description = "Retrieve all users in the system. Requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required")
    })
    public List<UserDto.Get> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/getUsersOfRole")
    @Operation(summary = "Get users by role", description = "Retrieve all users with a specific role. Requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role parameter"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required")
    })
    public List<UserDto.Get> getUsersOfARole(
            @Parameter(description = "Role to filter users by (USER, ADMIN)") @RequestParam Role role) {
        return userService.getAllUsersOfARole(role);
    }

    @GetMapping("/getUsersOfVerification")
    @Operation(summary = "Get users by verification status", description = "Retrieve all users filtered by verification status. Requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid verification parameter"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required")
    })
    public List<UserDto.Get> getUsersOfVerification(
            @Parameter(description = "Verification status to filter by (true for verified, false for unverified)") @RequestParam boolean isVerified) {
        return userService.getAllVerifiedOrUnverifiedUsers(isVerified);
    }

    @GetMapping("/getInactiveUsersSince")
    @Operation(summary = "Get inactive users", description = "Retrieve users who have been inactive since a specific date. Requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inactive users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date parameter"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required")
    })
    public List<UserDto.Get> getUsersInactiveSince(
            @Parameter(description = "Date to check for user inactivity (ISO format)") @RequestParam LocalDateTime date) {
        return userService.getInactiveUsersSince(date);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their UUID. Requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserDto.Get getUserById(
            @Parameter(description = "UUID of the user to retrieve") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.isAdmin(authentication)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        return userService.getUserById(userId);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve the currently authenticated user's profile information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public UserDto.Get getCurrentUser(
            @Parameter(hidden = true) Authentication authentication) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        return userService.getUserById(currentUserId);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve a specific user by their email address. Requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserDto.Get getUserByEmail(
            @Parameter(description = "Email address of the user to retrieve") @PathVariable String email,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.isAdmin(authentication)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        return userService.getUserByEmail(email);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve a specific user by their username. Requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid username format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserDto.Get getUserByUsername(
            @Parameter(description = "Username of the user to retrieve") @PathVariable String username,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.isAdmin(authentication)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        return userService.getUserByUsername(username);
    }

    @PostMapping("/createUser")
    @Operation(summary = "Create new user", description = "Create a new user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<UserDto.Get> createNewUser(
            @Parameter(description = "User creation data containing email, name, username, and password") @RequestBody UserDto.Create userDto) {
        UserDto.Get newUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PutMapping("/{userId}/verify")
    @Operation(summary = "Verify user", description = "Mark a user account as verified.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto.Get> verifyUser(
            @Parameter(description = "UUID of the user to verify") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.isAdmin(authentication)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        UserDto.Get user = userService.verifyUser(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/update")
    @Operation(summary = "Update user", description = "Update user information (name, email, username). Requires admin privileges or own user access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or user ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto.Get> updateUser(
            @Parameter(description = "UUID of the user to update") @PathVariable UUID userId,
            @Parameter(description = "User update data containing optional email, name, and username") @RequestBody UserDto.Update userDto,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.canAccess(authentication, userId)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        UserDto.Get updatedUser = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/updateLogin")
    @Operation(summary = "Update user login timestamp", description = "Update the user's last login timestamp to current time. Requires admin privileges or own user access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login timestamp updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> updateUserLogin(
            @Parameter(description = "UUID of the user to update login timestamp for") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.canAccess(authentication, userId)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        userService.updateLastLogin(userId);
        return ResponseEntity.ok("Successfully updated user's last login");
    }

    @PutMapping("/{userId}/updatePassword")
    @Operation(summary = "Update user password", description = "Change user's password with current password verification. Requires admin privileges or own user access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data, password requirements not met, or passwords don't match"),
            @ApiResponse(responseCode = "401", description = "User not authenticated or current password incorrect"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto.Get> updateUserPassword(
            @Parameter(description = "UUID of the user to update password for") @PathVariable UUID userId,
            @Parameter(description = "Password change data containing current password, new password, and confirmation") @RequestBody UserDto.ChangePassword userDto,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.canAccess(authentication, userId)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        UserDto.Get updatedUser = userService.updateUserPassword(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete/{userId}")
    @Operation(summary = "Delete user", description = "Permanently delete a user account and all associated data. Requires admin privileges or own user access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "UUID of the user to delete") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.canAccess(authentication, userId)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
