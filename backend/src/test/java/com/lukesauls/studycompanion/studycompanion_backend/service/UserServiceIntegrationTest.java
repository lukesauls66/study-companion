package com.lukesauls.studycompanion.studycompanion_backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.lukesauls.studycompanion.studycompanion_backend.dto.UserDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.user.InvalidPasswordChangeException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.user.InvalidUserCreationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.user.InvalidUserUpdateException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.user.UserAlreadyExistsException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.user.UserNotFoundException;
import com.lukesauls.studycompanion.studycompanion_backend.model.Role;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.User;

@SpringBootTest
@Transactional
@Rollback
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(createDto);

        assertThat(user.getEmail()).isEqualTo("test@email.com");
        assertThat(user.getName()).isEqualTo("John Smith");
        assertThat(user.getUsername()).isEqualTo("john123");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void createUser_BlankEmail_ThrowsInvalidUserCreationException() {
        UserDto.Create createDto = new UserDto.Create(" ", "John Smith", "john123", "password");

        InvalidUserCreationException exception = assertThrows(InvalidUserCreationException.class, () -> {
            userService.createUser(createDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Email cannot be empty");
    }

    @Test
    void createUser_BlankName_ThrowsInvalidUserCreationException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", " ", "john123", "password");

        InvalidUserCreationException exception = assertThrows(InvalidUserCreationException.class, () -> {
            userService.createUser(createDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Name cannot be empty");
    }

    @Test
    void createUser_BlankUsername_ThrowsInvalidUserCreationException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", " ", "password");

        InvalidUserCreationException exception = assertThrows(InvalidUserCreationException.class, () -> {
            userService.createUser(createDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Username cannot be empty");
    }

    @Test
    void createUser_BlankPassword_ThrowsInvalidUserCreationException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", " ");

        InvalidUserCreationException exception = assertThrows(InvalidUserCreationException.class, () -> {
            userService.createUser(createDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Password cannot be empty");
    }    
    
    @Test
    void createUser_DuplicateEmail_ThrowsUserAlreadyExistsException() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test@email.com", "Jane Smith", "jane123", "password2");

        userService.createUser(createDto1);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(createDto2);
        });

        assertThat(exception.getMessage()).isEqualTo("User with this email already exists");
    }

    @Test
    @SuppressWarnings("null")
    void getUserById_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User createdUser = userService.createUser(createDto);
        User user = userService.getUserById(createdUser.getId());

        assertThat(user.getEmail()).isEqualTo("test@email.com");
        assertThat(user.getName()).isEqualTo("John Smith");
        assertThat(user.getUsername()).isEqualTo("john123");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    @SuppressWarnings("null")
    void getUserById_NonExistentId_ThrowsUserNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(nonExistentId);
        });

        assertThat(exception.getMessage()).contains("User with ID " + nonExistentId + " not found");
    }

    @Test
    @SuppressWarnings("null")
    void getUserByEmail_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User createdUser = userService.createUser(createDto);
        User user = userService.getUserByEmail(createdUser.getEmail());

        assertThat(user.getEmail()).isEqualTo("test@email.com");
        assertThat(user.getName()).isEqualTo("John Smith");
        assertThat(user.getUsername()).isEqualTo("john123");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void getUserByEmail_NonExistentId_ThrowsUserNotFoundException() {
        String emailString = "example@gmail.com";

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByEmail(emailString);
        });

        assertThat(exception.getMessage()).contains("User with that email not found");
    }

    @Test
    @SuppressWarnings("null")
    void getUserByUsername_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User createdUser = userService.createUser(createDto);
        User user = userService.getUserByUsername(createdUser.getUsername());

        assertThat(user.getEmail()).isEqualTo("test@email.com");
        assertThat(user.getName()).isEqualTo("John Smith");
        assertThat(user.getUsername()).isEqualTo("john123");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    @SuppressWarnings("null")
    void getUserByUsername_NonExistentId_ThrowsUserNotFoundException() {
        String usernameString = "example123";

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByUsername(usernameString);
        });

        assertThat(exception.getMessage()).contains("User with that username not found");
    }

    @Test
    void getAllUsers_ReturnsUsers() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        userService.createUser(createDto1);
        userService.createUser(createDto2);

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getEmail()).isEqualTo("test@email.com");
        assertThat(users.get(0).getName()).isEqualTo("John Smith");
        assertThat(users.get(0).getUsername()).isEqualTo("john123");
        assertThat(users.get(0).getPassword()).isEqualTo("password1");
        assertThat(users.get(1).getEmail()).isEqualTo("test2@email.com");
        assertThat(users.get(1).getName()).isEqualTo("Jane Smith");
        assertThat(users.get(1).getUsername()).isEqualTo("jane123");
        assertThat(users.get(1).getPassword()).isEqualTo("password2");
    }

    @Test
    void getAllUsersOfARole_ReturnsUsers() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        userService.createUser(createDto1);
        userService.createUser(createDto2);

        List<User> users = userService.getAllUsersOfARole(Role.USER);
        List<User> admin = userService.getAllUsersOfARole(Role.ADMIN);

        assertThat(users).hasSize(2);
        assertThat(admin).hasSize(0);
    }

    @Test
    @SuppressWarnings("null")
    void getAllUsersOfVerifiedStatus_ReturnsUsers() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");
        UserDto.Create createDto3 = new UserDto.Create("test3@email.com", "Jacob Smith", "jacob123", "password3");

        User user1 = userService.createUser(createDto1);
        userService.createUser(createDto2);
        User user3 = userService.createUser(createDto3);

        userService.verifyUser(user1.getId());
        userService.verifyUser(user3.getId());

        List<User> verifiedUsers = userService.getAllVerifiedOrUnverifiedUsers(true);
        List<User> unverifiedUsers = userService.getAllVerifiedOrUnverifiedUsers(false);

        assertThat(verifiedUsers).hasSize(2);
        assertThat(unverifiedUsers).hasSize(1);
    }

    @Test
    @SuppressWarnings("null")
    void getAllUsersInactiveSinceDate_ReturnsUsers() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        User user1 = userService.createUser(createDto1);
        User user2 = userService.createUser(createDto2);

        userService.updateLastLogin(user1.getId());
        userService.updateLastLogin(user2.getId());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<User> users = userService.getInactiveUsersSince(LocalDateTime.now());

        assertThat(users).hasSize(2);
    }

    @Test
    @SuppressWarnings("null")
    void updateUserEmail_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");

        User user = userService.createUser(createDto);

        UserDto.Update dto = new UserDto.Update("newtest@email.com", null, null);

        userService.updateUser(user.getId(), dto);
        User refreshedUser = userService.getUserById(user.getId());

        assertThat(refreshedUser.getEmail()).isEqualTo("newtest@email.com");
        assertThat(refreshedUser.getName()).isEqualTo("John Smith");
        assertThat(refreshedUser.getUsername()).isEqualTo("john123");
    }

    @Test
    @SuppressWarnings("null")
    void updateUserEmail_InvalidInput_ThrowsException() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        User user = userService.createUser(createDto1);
        userService.createUser(createDto2);

        UserDto.Update dto1 = new UserDto.Update("test@email.com", null, null);
        UserDto.Update dto2 = new UserDto.Update("test2@email.com", null, null);

        InvalidUserUpdateException exception1 = assertThrows(InvalidUserUpdateException.class, () -> {
            userService.updateUser(user.getId(), dto1);
        });

        assertThat(exception1.getMessage()).isEqualTo("Email is already set to this value");

        UserAlreadyExistsException exception2 = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.updateUser(user.getId(), dto2);
        });

        assertThat(exception2.getMessage()).isEqualTo("User with this email already exists");
    }

    @Test
    @SuppressWarnings("null")
    void updateUserNameAndUserName_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");

        User user = userService.createUser(createDto);

        UserDto.Update dto = new UserDto.Update(null, "Jacob Smith", "jacob123");

        userService.updateUser(user.getId(), dto);
        User refreshedUser = userService.getUserById(user.getId());

        assertThat(refreshedUser.getEmail()).isEqualTo("test@email.com");
        assertThat(refreshedUser.getName()).isEqualTo("Jacob Smith");
        assertThat(refreshedUser.getUsername()).isEqualTo("jacob123");
    }

    @Test
    @SuppressWarnings("null")
    void updateUser_InvalidInput_ThrowsInvalidUserUpdate() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");

        User user = userService.createUser(createDto);

        UserDto.Update updateDto = new UserDto.Update(null, " ", " ");

        InvalidUserUpdateException exception = assertThrows(InvalidUserUpdateException.class, () -> {
            userService.updateUser(user.getId(), updateDto);
        });

        assertThat(exception.getMessage()).isEqualTo("At least one field must be provided");
    }

    @Test
    @SuppressWarnings("null")
    void updateUserPassword_ValidInput_UpdatesPassword() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(createDto);

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("password", "newpassword", "newpassword");

        User updatedUser = userService.updateUserPassword(user.getId(), changeDto);

        assertThat(updatedUser.getPassword()).isEqualTo("newpassword");
        assertThat(updatedUser.getEmail()).isEqualTo("test@email.com");
    }

    @Test
    @SuppressWarnings("null")
    void updateUserPassword_WrongCurrentPassword_ThrowsInvalidPasswordChangeException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(createDto);

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("wrongpassword", "newpassword", "newpassword");

        InvalidPasswordChangeException exception = assertThrows(InvalidPasswordChangeException.class, () -> {
            userService.updateUserPassword(user.getId(), changeDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Current password is incorrect");
    }

    @Test
    @SuppressWarnings("null")
    void updateUserPassword_SamePassword_ThrowsInvalidPasswordChangeException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(createDto);

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("password", "password", "password");

        InvalidPasswordChangeException exception = assertThrows(InvalidPasswordChangeException.class, () -> {
            userService.updateUserPassword(user.getId(), changeDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Cannot change password to existing password");
    }

    @Test
    @SuppressWarnings("null")
    void updateUserPassword_PasswordMismatch_ThrowsInvalidPasswordChangeException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(createDto);

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("password", "newpassword", "differentpassword");

        InvalidPasswordChangeException exception = assertThrows(InvalidPasswordChangeException.class, () -> {
            userService.updateUserPassword(user.getId(), changeDto);
        });

        assertThat(exception.getMessage()).isEqualTo("New password doesn't match confirm password");
    }

    @Test
    @SuppressWarnings("null")
    void updateUserPassword_NonExistentUser_ThrowsUserNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("password", "newpassword", "newpassword");

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUserPassword(nonExistentId, changeDto);
        });

        assertThat(exception.getMessage()).contains("User with ID " + nonExistentId + " not found");
    }

    @Test
    @SuppressWarnings("null")
    void deleteUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(createDto);
        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(1);

        userService.deleteUser(user.getId());
        users = userService.getAllUsers();

        assertThat(users).hasSize(0);
    }
}
