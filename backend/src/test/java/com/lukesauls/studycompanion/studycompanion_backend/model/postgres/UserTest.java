package com.lukesauls.studycompanion.studycompanion_backend.model.postgres;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.lukesauls.studycompanion.studycompanion_backend.model.Role;

class UserTest {
    
    @Test
    void testUser1Creation() {
        User user = new User();
        user.setName("Test User");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        assertEquals("Test User", user.getName());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertFalse(user.isVerified());
        user.setVerified(true);
        assertTrue(user.isVerified());
    }

    @Test
    void testUser2Creation() {
        User user = new User("test2@example.com", "Test User 2", "testuser2", "password123");
        assertEquals("Test User 2", user.getName());
        assertEquals("testuser2", user.getUsername());
        assertEquals("test2@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertFalse(user.isVerified());
        user.setVerified(true);
        assertTrue(user.isVerified());
    }
}
