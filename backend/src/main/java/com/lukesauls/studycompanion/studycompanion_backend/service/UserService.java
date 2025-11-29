package com.lukesauls.studycompanion.studycompanion_backend.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lukesauls.studycompanion.studycompanion_backend.dto.UserDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UserAlreadyExistsException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UserNotFoundException;
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
    //FIXME: Add password encoding when adding auth
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
    public User getUserById(@NonNull UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }

        return userRepository.findById(id).get();
    }
}
