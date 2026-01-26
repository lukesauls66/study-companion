package com.study_companion.backend.model.postgres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.study_companion.backend.model.Role;

class UserTest {

    private String name;
    private String username;
    private String email;
    private String password;

    @BeforeEach
    void setUp() {
        name = "Test User";
        username = "testuser";
        email = "test@example.com";
        password = "password123";
    }

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNotNull(user);
        assertNull(user.getName());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertFalse(user.isVerified());
    }

    @Test
    void testParameterizedConstructor() {
        User user = new User(email, name, username, password);
        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertFalse(user.isVerified());
    }

    @Test
    void testGettersAndSetters() {
        User user = new User();
        
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.ADMIN);
        user.setVerified(true);
        
        assertEquals(name, user.getName());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
        assertTrue(user.isVerified());
    }

    @Test
    void testRoleEnumValues() {
        User user = new User();
        assertEquals(Role.USER, user.getRole());

        user.setRole(Role.ADMIN);
        assertEquals(Role.ADMIN, user.getRole());
    }
}
