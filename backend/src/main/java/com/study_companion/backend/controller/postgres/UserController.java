package com.study_companion.backend.controller.postgres;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study_companion.backend.dto.UserDto; 
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
}
