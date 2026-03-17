package com.study_companion.backend.model.postgres;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.study_companion.backend.model.FileType;
import com.study_companion.backend.model.ParsingStatus;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UploadTest {

    @Mock
    private Deck mockDeck;
    private User mockUser;

    private String fileName;
    private String fileUrl;
    private FileType fileType;
    private Long fileSize;
    private String errorMessage;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockDeck = new Deck();
        fileName = "testfile.png";
        fileUrl = "https://example.com/testfile.png";
        fileType = FileType.PNG;
        fileSize = 2048L;
        errorMessage = "Sample error message";
    }

    @Test
    void testDefaultConstructor() {
        Upload upload = new Upload();
        assertNotNull(upload);
        assertNull(upload.getUser());
        assertNull(upload.getDeck());
        assertNull(upload.getFileName());
        assertNull(upload.getFileUrl());
        assertNull(upload.getFileType());
        assertNull(upload.getFileSize());
        assertFalse(upload.getIsParsed());
        assertNull(upload.getParsingStatus());
        assertNull(upload.getErrorMessage());
    }

    @Test
    void testParameterizedConstructor() {
        Upload upload = new Upload(mockUser, mockDeck, fileName, fileUrl, fileType, fileSize);
        assertNotNull(upload);
        assertEquals(mockUser, upload.getUser());
        assertEquals(mockDeck, upload.getDeck());
        assertEquals(fileName, upload.getFileName());
        assertEquals(fileUrl, upload.getFileUrl());
        assertEquals(fileType, upload.getFileType());
        assertEquals(fileSize, upload.getFileSize());
        assertFalse(upload.getIsParsed());
        assertEquals(ParsingStatus.PENDING, upload.getParsingStatus());
        assertNull(upload.getErrorMessage());
    }

    @Test
    void testGettersAndSetters() {
        Upload upload = new Upload();

        upload.setUser(mockUser);
        upload.setDeck(mockDeck);
        upload.setFileName(fileName);
        upload.setFileUrl(fileUrl);
        upload.setFileType(fileType);
        upload.setFileSize(fileSize);
        upload.setErrorMessage(errorMessage);

        assertEquals(mockUser, upload.getUser());
        assertEquals(mockDeck, upload.getDeck());
        assertEquals(fileName, upload.getFileName());
        assertEquals(fileUrl, upload.getFileUrl());
        assertEquals(fileType, upload.getFileType());
        assertEquals(fileSize, upload.getFileSize());
    }

    @Test
    void testStartParsing() {
        Upload upload = new Upload();
        upload.startParsing();
        assertEquals(ParsingStatus.PROCESSING, upload.getParsingStatus());
    }

    @Test
    void testCompleteParsingSuccess() {
        Upload upload = new Upload();
        upload.completeParsing();
        assertEquals(ParsingStatus.COMPLETED, upload.getParsingStatus());
        assertTrue(upload.getIsParsed());
        assertNull(upload.getErrorMessage());
    }

    @Test
    void testFailParsing() {
        Upload upload = new Upload();
        String error = "Parsing failed due to an error.";
        upload.failParsing(error);
        assertEquals(ParsingStatus.FAILED, upload.getParsingStatus());
        assertFalse(upload.getIsParsed());
        assertEquals(error, upload.getErrorMessage());
    }

    @Test
    void testFileTypeEnumValues() {
        Upload upload = new Upload();

        upload.setFileType(FileType.JPG);
        assertEquals(FileType.JPG, upload.getFileType());

        upload.setFileType(FileType.JPEG);
        assertEquals(FileType.JPEG, upload.getFileType());

        upload.setFileType(FileType.PNG);
        assertEquals(FileType.PNG, upload.getFileType());
    }
}
