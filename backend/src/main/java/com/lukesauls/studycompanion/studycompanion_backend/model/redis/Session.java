package com.lukesauls.studycompanion.studycompanion_backend.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@RedisHash(value = "session", timeToLive = 86400)
public class Session implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String sessionId;

    @Indexed
    @NotNull
    private UUID userId;

    @NotNull
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    private LocalDateTime lastAccessedAt;

    // Constructors
    public Session() {
        this.sessionId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }

    public Session(UUID userId, long ttlSeconds) {
        this();
        this.userId = userId;
        this.expiresAt = LocalDateTime.now().plusSeconds(ttlSeconds);
    }

    public Session(UUID userId) {
        this(userId, 86400);
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void extendSession(long additionalSeconds) {
        this.expiresAt = this.expiresAt.plusSeconds(additionalSeconds);
    }

    public void extendSession() {
        this.expiresAt = this.expiresAt.plusSeconds(86400); // Default to 24 hours
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", userId=" + userId +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", lastAccessedAt=" + lastAccessedAt +
                '}';
    }
}