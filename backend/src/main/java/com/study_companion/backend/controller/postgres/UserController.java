package com.study_companion.backend.controller.postgres;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.study_companion.backend.model.Role;
import com.study_companion.backend.service.UserService;

@RestController
@RequestMapping(value = "/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getUsers")
    public List<UserDto.Get> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/getUsersOfRole")
    public List<UserDto.Get> getUsersOfARole(@RequestParam Role role) {
        return userService.getAllUsersOfARole(role);
    }

    @GetMapping("/getUsersOfVerification")
    public List<UserDto.Get> getUsersOfVerification(@RequestParam boolean isVerified) {
        return userService.getAllVerifiedOrUnverifiedUsers(isVerified);
    }

    @GetMapping("/getInactiveUsersSince")
    public List<UserDto.Get> getUsersInactiveSince(@RequestParam LocalDateTime date) {
        return userService.getInactiveUsersSince(date);
    }

    @GetMapping("/{userId}")
    public UserDto.Get getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/email/{email}")
    public UserDto.Get getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping("/username/{username}")
    public UserDto.Get getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @PostMapping("/createUser")
    public ResponseEntity<UserDto.Get> createNewUser(@RequestBody UserDto.Create userDto) {
        UserDto.Get newUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PutMapping("/{userId}/verify")
    public ResponseEntity<UserDto.Get> verifyUser(@PathVariable UUID userId) {
        UserDto.Get user = userService.verifyUser(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<UserDto.Get> updateUser(@PathVariable UUID userId, @RequestBody UserDto.Update userDto) {
        UserDto.Get updatedUser = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/updateLogin")
    public ResponseEntity<String> updateUserLogin(@PathVariable UUID userId) {
        userService.updateLastLogin(userId);
        return ResponseEntity.ok("Successfully updated user's last login");
    }

    @PutMapping("/{userId}/updatePassword")
    public ResponseEntity<UserDto.Get> updateUserPassword(@PathVariable UUID userId,
            @RequestBody UserDto.ChangePassword userDto) {
        UserDto.Get updatedUser = userService.updateUserPassword(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
