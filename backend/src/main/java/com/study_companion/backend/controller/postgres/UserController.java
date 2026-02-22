package com.study_companion.backend.controller.postgres;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    // FIXME: POST 201 with return
    // ResponseEntity.status(HttpStatus.CREATED).body(createdUser), PUT 200 with
    // return ResponseEntity.ok(updatedDeck), DELETE 204 with return
    // ResponseEntity.noContent().build()
 
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
    public ResponseEntity<String> createNewUser(@RequestBody UserDto.Create userDto) {
        userService.createUser(userDto); 
        return ResponseEntity.ok("User successfully created");
    }

    @PostMapping("/{userId}/verify")
    public ResponseEntity<String> verifyUser(@PathVariable UUID userId) {
        userService.verifyUser(userId);
        return ResponseEntity.ok("User successfully verified");
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<String> updateUser(@PathVariable UUID userId, @RequestBody UserDto.Update userDto) {
        userService.updateUser(userId, userDto);
        return ResponseEntity.ok("Successfully updated user");
    }

    @PutMapping("/{userId}/updateLogin")
    public ResponseEntity<String> updateUserLogin(@PathVariable UUID userId) {
        userService.updateLastLogin(userId);
        return ResponseEntity.ok("Successfully updated user's last login");
    }

    @PutMapping("/{userId}/updatePassword")
    public ResponseEntity<String> updateUserPassword(@PathVariable UUID userId, @RequestBody UserDto.ChangePassword userDto) {
        userService.updateUserPassword(userId, userDto);
        return ResponseEntity.ok("User successfully updated password"); 
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("Successfully deleted user");
    }
}
