package com.study_companion.backend.model.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.time.LocalDateTime;

public class SessionTest {
    
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void testDefaultConstructor() {
        Session session = new Session();
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertNull(session.getUserId());
        assertNull(session.getExpiresAt());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getLastAccessedAt());
    }

    @Test
    void testParameterizedConstructorWithTTL() {
        long ttlSeconds = 7200; // 2 hours
        Session session = new Session(userId, ttlSeconds);
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertEquals(userId, session.getUserId());
        assertNotNull(session.getExpiresAt());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getLastAccessedAt());
        assertTrue(session.getExpiresAt().isAfter(session.getCreatedAt()));
    }

    @Test
    void testParameterizedConstructorWithoutTTL() {
        Session session = new Session(userId);
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertEquals(userId, session.getUserId());
        assertNotNull(session.getExpiresAt());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getLastAccessedAt());
        assertTrue(session.getExpiresAt().isAfter(session.getCreatedAt()));
    }

    @Test
    void testGettersAndSetters() {
        Session session = new Session(userId);

        assertEquals(userId, session.getUserId());

        LocalDateTime beforeUpdate = session.getLastAccessedAt();
        session.setLastAccessedAt();
        LocalDateTime afterUpdate = session.getLastAccessedAt();

        assertTrue(afterUpdate.isAfter(beforeUpdate) || afterUpdate.isEqual(beforeUpdate));
    }

    @Test
    void testIsExpired() {
        Session session = new Session(userId, 1); // 1 second TTL
        assertFalse(session.isExpired());
        try {
            Thread.sleep(1500); // Wait for 1.5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(session.isExpired());
    }

    @Test
    void testExtendSessionWithAdditionalSeconds() {
        Session session = new Session(userId, 1); // 1 second TTL
        LocalDateTime originalExpiry = session.getExpiresAt();
        session.extendSession(5); // Extend by 5 seconds
        assertTrue(session.getExpiresAt().isAfter(originalExpiry));
    }

    @Test
    void testExtendSessionWithDefaultSeconds() {
        Session session = new Session(userId, 1); // 1 second TTL
        LocalDateTime originalExpiry = session.getExpiresAt();
        session.extendSession(); // Extend by default 86400 seconds (24 hours)
        assertTrue(session.getExpiresAt().isAfter(originalExpiry));
    }

    @Test
    void testToString() {
        Session session = new Session(userId);
        
        String expectedString = "Session{" +
                "sessionId='" + session.getSessionId() + '\'' +
                ", userId='" + session.getUserId() + '\'' +
                ", expiresAt=" + session.getExpiresAt() +
                ", createdAt=" + session.getCreatedAt() +
                ", lastAccessedAt=" + session.getLastAccessedAt() +
                '}';
        assertEquals(expectedString, session.toString());
    }
}