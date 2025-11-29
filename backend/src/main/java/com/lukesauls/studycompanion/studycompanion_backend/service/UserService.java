package com.lukesauls.studycompanion.studycompanion_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lukesauls.studycompanion.studycompanion_backend.dto.UserDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UserAlreadyExistsException;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.User;
import com.lukesauls.studycompanion.studycompanion_backend.repository.postgres.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Create new user if user doesn't already exist
     */
    public User createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.email()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        User user = new User(userDto.email(), userDto.name(), userDto.username(), userDto.password());
        
        return userRepository.save(user);
    }
}
