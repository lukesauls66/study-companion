package com.study_companion.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.study_companion.backend.dto.UserDto;
import com.study_companion.backend.exception.user.InvalidPasswordChangeException;
import com.study_companion.backend.exception.user.InvalidUserCreationException;
import com.study_companion.backend.exception.user.InvalidUserParameterException;
import com.study_companion.backend.exception.user.InvalidUserUpdateException;
import com.study_companion.backend.exception.user.UnauthorizedUserAccessException;
import com.study_companion.backend.exception.user.UserAlreadyExistsException;
import com.study_companion.backend.exception.user.UserNotFoundException;
import com.study_companion.backend.model.Role;

@SpringBootTest
@Transactional
@Rollback
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;
 
    @Test
    void createUser_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get user = userService.createUser(createDto);

        assertThat(user.email()).isEqualTo("test@email.com");
        assertThat(user.name()).isEqualTo("John Smith");
        assertThat(user.username()).isEqualTo("john123");
    }

    @Test
    void createUser_NullUserDto_ThrowsInvalidUserParameterException() {
        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.createUser(null);
        });

        assertThat(exception.getMessage()).isEqualTo("User data cannot be null when creating a user");
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
    void getUserById_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get createdUser = userService.createUser(createDto);
        UserDto.Get user = userService.getUserById(createdUser.id());
 
        assertThat(user.email()).isEqualTo("test@email.com");
        assertThat(user.name()).isEqualTo("John Smith");
        assertThat(user.username()).isEqualTo("john123");
    }

    @Test
    void getUserById_NullId_ThrowsInvalidUserParameterException() {
        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.getUserById(null);
        });

        assertThat(exception.getMessage()).isEqualTo("ID cannot be null");
    }

    @Test
    void getUserById_NonExistentId_ThrowsUserNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(nonExistentId);
        });

        assertThat(exception.getMessage()).contains("User with ID " + nonExistentId + " not found");
    }

    @Test
    void getUserByEmail_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get createdUser = userService.createUser(createDto);
        UserDto.Get user = userService.getUserByEmail(createdUser.email());

        assertThat(user.email()).isEqualTo("test@email.com");
        assertThat(user.name()).isEqualTo("John Smith");
        assertThat(user.username()).isEqualTo("john123");
    }

    @Test
    void getUserByEmail_NullEmail_ThrowsInvalidUserParameterException() {
        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.getUserByEmail(null);
        });

        assertThat(exception.getMessage()).isEqualTo("Email cannot be null");
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
    void getUserByUsername_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get createdUser = userService.createUser(createDto);
        UserDto.Get user = userService.getUserByUsername(createdUser.username());

        assertThat(user.email()).isEqualTo("test@email.com");
        assertThat(user.name()).isEqualTo("John Smith");
        assertThat(user.username()).isEqualTo("john123");
    }

    @Test
    void getUserByUsername_NullUsername_ThrowsInvalidUserParameterException() {
        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.getUserByUsername(null);
        });

        assertThat(exception.getMessage()).isEqualTo("Username cannot be null");
    }

    @Test
    void getUserByUsername_NonExistentId_ThrowsUserNotFoundException() {
        String usernameString = "example123";

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByUsername(usernameString);
        });

        assertThat(exception.getMessage()).contains("User with that username not found");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ValidInput_ReturnsUsers() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        userService.createUser(createDto1);
        userService.createUser(createDto2);

        List<UserDto.Get> users = userService.getAllUsers();

        assertThat(users).hasSize(2); 
        assertThat(users.get(0).email()).isEqualTo("test@email.com");
        assertThat(users.get(0).name()).isEqualTo("John Smith");
        assertThat(users.get(0).username()).isEqualTo("john123");
        assertThat(users.get(1).email()).isEqualTo("test2@email.com"); 
        assertThat(users.get(1).name()).isEqualTo("Jane Smith");
        assertThat(users.get(1).username()).isEqualTo("jane123");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_NonAdmin_ThrowsUnauthorizedUserAccessException() {
        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () -> {
            userService.getAllUsers();
        });

        assertThat(exception.getMessage()).isEqualTo("Unauthorized user access");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersOfARole_ValidInput_ReturnsUsers() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        userService.createUser(createDto1);
        userService.createUser(createDto2);

        List<UserDto.Get> users = userService.getAllUsersOfARole(Role.USER);
        List<UserDto.Get> admin = userService.getAllUsersOfARole(Role.ADMIN);

        assertThat(users).hasSize(2);
        assertThat(admin).hasSize(0); 
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersOfARole_NullRole_ThrowsInvalidUserParameterException() {
        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.getAllUsersOfARole(null);
        });

        assertThat(exception.getMessage()).isEqualTo("Roll cannot be null");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsersOfARole_NonAdmin_ThrowsUnauthorizedUserAccessException() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        userService.createUser(createDto1);
        userService.createUser(createDto2);

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () -> {
            userService.getAllUsersOfARole(Role.USER);
        });

        assertThat(exception.getMessage()).isEqualTo("Unauthorized user access");
    }

    @Test
    void verifyUser_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
    
        UserDto.Get user1 = userService.createUser(createDto);

        UserDto.Get verifiedUser = userService.verifyUser(user1.id());

        assertThat(verifiedUser.isVerified()).isTrue();
    }

    @Test
    void verifyUser_NullUserId_ThrowsInvalidUserParameterException() {
        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.verifyUser(null);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    void getAllUsersOfVerifiedStatus_ValidInput_ReturnsUsers() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");
        UserDto.Create createDto3 = new UserDto.Create("test3@email.com", "Jacob Smith", "jacob123", "password3");

        UserDto.Get user1 = userService.createUser(createDto1);
        userService.createUser(createDto2);
        UserDto.Get user3 = userService.createUser(createDto3);

        userService.verifyUser(user1.id());
        userService.verifyUser(user3.id());

        List<UserDto.Get> verifiedUsers = userService.getAllVerifiedOrUnverifiedUsers(true);
        List<UserDto.Get> unverifiedUsers = userService.getAllVerifiedOrUnverifiedUsers(false);

        assertThat(verifiedUsers).hasSize(2);
        assertThat(unverifiedUsers).hasSize(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersInactiveSinceDate_ValidInput_ReturnsUsers() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        UserDto.Get user1 = userService.createUser(createDto1);
        UserDto.Get user2 = userService.createUser(createDto2);

        userService.updateLastLogin(user1.id());
        userService.updateLastLogin(user2.id());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<UserDto.Get> users = userService.getInactiveUsersSince(LocalDateTime.now());

        assertThat(users).hasSize(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersInactiveSinceDate_NullDate_ThrowsInvalidUserParameterException() {
        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.getInactiveUsersSince(null);
        });

        assertThat(exception.getMessage()).isEqualTo("Date cannot be null");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsersInactiveSinceDate_NonAdmin_ThrowsUnauthorizedUserAccessException() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        UserDto.Get user1 = userService.createUser(createDto1);
        UserDto.Get user2 = userService.createUser(createDto2);

        userService.updateLastLogin(user1.id());
        userService.updateLastLogin(user2.id());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () -> {
            userService.getInactiveUsersSince(LocalDateTime.now());
        });

        assertThat(exception.getMessage()).isEqualTo("Unauthorized user access");
    }

    @Test
    void updateUserEmail_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");

        UserDto.Get user = userService.createUser(createDto);

        UserDto.Update dto = new UserDto.Update("newtest@email.com", null, null);

        userService.updateUser(user.id(), dto);
        UserDto.Get refreshedUser = userService.getUserById(user.id());

        assertThat(refreshedUser.email()).isEqualTo("newtest@email.com");
        assertThat(refreshedUser.name()).isEqualTo("John Smith");
        assertThat(refreshedUser.username()).isEqualTo("john123");
    }

    @Test
    void updateUserEmail_InvalidInput_ThrowsException() {
        UserDto.Create createDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");
        UserDto.Create createDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        UserDto.Get user = userService.createUser(createDto1);
        userService.createUser(createDto2);

        UserDto.Update dto1 = new UserDto.Update("test@email.com", null, null);
        UserDto.Update dto2 = new UserDto.Update("test2@email.com", null, null);

        InvalidUserUpdateException exception1 = assertThrows(InvalidUserUpdateException.class, () -> {
            userService.updateUser(user.id(), dto1);
        });

        assertThat(exception1.getMessage()).isEqualTo("Email is already set to this value");

        UserAlreadyExistsException exception2 = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.updateUser(user.id(), dto2);
        });

        assertThat(exception2.getMessage()).isEqualTo("User with this email already exists");
    }

    @Test
    void updateUserNameAndUserName_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");

        UserDto.Get user = userService.createUser(createDto);

        UserDto.Update dto = new UserDto.Update(null, "Jacob Smith", "jacob123");

        userService.updateUser(user.id(), dto);
        UserDto.Get refreshedUser = userService.getUserById(user.id());

        assertThat(refreshedUser.email()).isEqualTo("test@email.com");
        assertThat(refreshedUser.name()).isEqualTo("Jacob Smith");
        assertThat(refreshedUser.username()).isEqualTo("jacob123");
    }

    @Test
    void updateUser_InvalidInput_ThrowsInvalidUserUpdate() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");

        UserDto.Get user = userService.createUser(createDto);

        UserDto.Update updateDto = new UserDto.Update(null, " ", " ");

        InvalidUserUpdateException exception = assertThrows(InvalidUserUpdateException.class, () -> {
            userService.updateUser(user.id(), updateDto);
        });

        assertThat(exception.getMessage()).isEqualTo("At least one field must be provided");
    }

    @Test
    void updateUser_NullUserId_ThrowsInvalidUserParameterException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");

        userService.createUser(createDto);

        UserDto.Update dto = new UserDto.Update("test@email.com", "Jacob Smith", "jacob123");

        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.updateUser(null, dto);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    void updateUser_NullUserDto_ThrowsInvalidUserParameterException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password1");

        UserDto.Get user = userService.createUser(createDto);

        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.updateUser(user.id(), null);
        });

        assertThat(exception.getMessage()).isEqualTo("User data transfer object cannot be null");
    }

    @Test
    void updateUserPassword_ValidInput_ReturnsUser() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get user = userService.createUser(createDto);

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("password", "newpassword", "newpassword");
    
        userService.updateUserPassword(user.id(), changeDto);
    }

    @Test
    void updateUserPassword_NullUserId_ThrowsInvalidUserParameterException() {
        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("password", "newpassword", "newpassword");
    
        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.updateUserPassword(null, changeDto);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    void updateUserPassword_NullUserDto_ThrowsInvalidUserParameterException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get user = userService.createUser(createDto);

        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.updateUserPassword(user.id(), null);
        });

        assertThat(exception.getMessage()).isEqualTo("User data transfer object cannot be null");
    }

    @Test
    void updateUserPassword_WrongCurrentPassword_ThrowsInvalidPasswordChangeException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get user = userService.createUser(createDto);

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("wrongpassword", "newpassword", "newpassword");
 
        InvalidPasswordChangeException exception = assertThrows(InvalidPasswordChangeException.class, () -> {
            userService.updateUserPassword(user.id(), changeDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Current password is incorrect");
    }

    @Test
    void updateUserPassword_SamePassword_ThrowsInvalidPasswordChangeException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get user = userService.createUser(createDto);

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("password", "password", "password");

        InvalidPasswordChangeException exception = assertThrows(InvalidPasswordChangeException.class, () -> {
            userService.updateUserPassword(user.id(), changeDto);
        }); 
 
        assertThat(exception.getMessage()).isEqualTo("Cannot change password to existing password");
    }

    @Test
    void updateUserPassword_PasswordMismatch_ThrowsInvalidPasswordChangeException() {
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get user = userService.createUser(createDto);

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("password", "newpassword", "differentpassword");

        InvalidPasswordChangeException exception = assertThrows(InvalidPasswordChangeException.class, () -> {
            userService.updateUserPassword(user.id(), changeDto);
        });

        assertThat(exception.getMessage()).isEqualTo("New password doesn't match confirm password");
    }

    @Test
    void updateUserPassword_NonExistentUser_ThrowsUserNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        UserDto.ChangePassword changeDto = new UserDto.ChangePassword("password", "newpassword", "newpassword");

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUserPassword(nonExistentId, changeDto);
        });

        assertThat(exception.getMessage()).contains("User with ID " + nonExistentId + " not found");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ValidInput() { 
        UserDto.Create createDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        UserDto.Get user = userService.createUser(createDto);
        List<UserDto.Get> users = userService.getAllUsers();

        assertThat(users).hasSize(1); 

        userService.deleteUser(user.id());
        users = userService.getAllUsers();
 
        assertThat(users).hasSize(0);
    }

    @Test
    void deleteUser_NullUserId_ThrowsInvalidUserParameterException() {
        InvalidUserParameterException exception = assertThrows(InvalidUserParameterException.class, () -> {
            userService.deleteUser(null);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }
}
  