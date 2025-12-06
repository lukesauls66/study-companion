package com.lukesauls.studycompanion.studycompanion_backend.model.postgres;

import com.lukesauls.studycompanion.studycompanion_backend.model.Role;
import jakarta.validation.constraints.*;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Size(max = 32)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Role role;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    private boolean isVerified;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deck> decks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Upload> uploads = new ArrayList<>();

    // Constructors
    public User() {
        this.role = Role.USER;
        this.isVerified = false;
    }

    public User(String email, String name, String username, String password) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = Role.USER;
        this.isVerified = false;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public List<Deck> getDecks() {
        return decks;
    }

    public List<Upload> getUploads() {
        return uploads;
    }

    //Helpers for managing bi-directional relationship
    public void addDeck(Deck deck) {
        decks.add(deck);
        deck.setUser(this);
    }

    public void removeDeck(Deck deck) {
        decks.remove(deck);
        deck.setUser(null);
    }

    public void addUpload(Upload upload) {
        uploads.add(upload);
        upload.setUser(this);
    }

    public void removeUpload(Upload upload) {
        uploads.remove(upload);
        upload.setUser(null);
    }

    //Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
