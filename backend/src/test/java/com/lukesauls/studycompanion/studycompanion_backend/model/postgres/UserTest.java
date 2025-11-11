package com.lukesauls.studycompanion.studycompanion_backend.model.postgres;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    
    @Test
    void testUser1Creation() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void testUser2Creation() {
        User user = new User("test2@example.com", "Test User 2", "testuser2", "password123");
        assertEquals("testuser2", user.getUsername());
        assertEquals("test2@example.com", user.getEmail());
    }
}
