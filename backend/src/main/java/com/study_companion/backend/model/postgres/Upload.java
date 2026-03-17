package com.study_companion.backend.model.postgres;

import com.study_companion.backend.model.FileType;
import com.study_companion.backend.model.ParsingStatus;
import jakarta.validation.constraints.*;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploads")
public class Upload {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deckId", nullable = false)
    private Deck deck;

    @NotBlank
    @Column(nullable = false)
    private String fileName;

    @NotBlank
    @Column(nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private FileType fileType;

    @NotNull
    @Column(nullable = false)
    private Long fileSize;

    private boolean isParsed;

    @Enumerated(EnumType.STRING)
    private ParsingStatus parsingStatus;

    private String errorMessage;

    private LocalDateTime parsingStartedAt;

    private LocalDateTime parsingCompletedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public Upload() {
    }

    public Upload(User user, Deck deck, String fileName, String fileUrl, FileType fileType, Long fileSize) {
        this.user = user;
        this.deck = deck;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.isParsed = false;
        this.parsingStatus = ParsingStatus.PENDING;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean getIsParsed() {
        return isParsed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public ParsingStatus getParsingStatus() {
        return parsingStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getParsingStartedAt() {
        return parsingStartedAt;
    }

    public LocalDateTime getParsingCompletedAt() {
        return parsingCompletedAt;
    }

    // Helper Methods
    public void startParsing() {
        this.parsingStatus = ParsingStatus.PROCESSING;
        this.parsingStartedAt = LocalDateTime.now();
    }

    public void completeParsing() {
        this.parsingStatus = ParsingStatus.COMPLETED;
        this.parsingCompletedAt = LocalDateTime.now();
        this.isParsed = true;
    }

    public void failParsing(String errorMessage) {
        this.parsingStatus = ParsingStatus.FAILED;
        this.parsingCompletedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.isParsed = false;
    }

    // Lifecycle Callbacks
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
