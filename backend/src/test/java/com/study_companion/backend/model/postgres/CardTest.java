package com.study_companion.backend.model.postgres;

import com.study_companion.backend.model.CardCreationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CardTest {
    
    @Mock
    private Deck mockDeck;
    
    private String testQuestion;
    private String testAnswer;
    private String testImageUrl;
    
    @BeforeEach
    void setUp() {
        testQuestion = "What is 2 + 2?";
        testAnswer = "4";
        testImageUrl = "https://example.com/image.jpg";
    }
    
    @Test
    void testCardConstructorWithoutImage() {
        CardCreationType creationType = CardCreationType.MANUAL_UPLOAD;
        
        Card card = new Card(mockDeck, testQuestion, testAnswer, creationType);
        assertNotNull(card);
        assertEquals(mockDeck, card.getDeck());
        assertEquals(testQuestion, card.getQuestion());
        assertEquals(testAnswer, card.getAnswer());
        assertEquals(creationType, card.getCreationType());
        assertNull(card.getImageUrl());
    }
    
    @Test
    void testCardConstructorWithImage() {
        CardCreationType creationType = CardCreationType.AI_PARSED;
        
        Card card = new Card(mockDeck, testQuestion, testAnswer, creationType, testImageUrl);
        assertNotNull(card);
        assertEquals(mockDeck, card.getDeck());
        assertEquals(testQuestion, card.getQuestion());
        assertEquals(testAnswer, card.getAnswer());
        assertEquals(creationType, card.getCreationType());
        assertEquals(testImageUrl, card.getImageUrl());
    }
    
    @Test
    void testDefaultConstructor() {
        Card card = new Card();
        assertNotNull(card);
        assertNull(card.getDeck());
        assertNull(card.getQuestion());
        assertNull(card.getAnswer()); 
        assertNull(card.getCreationType());
        assertNull(card.getImageUrl());
    }
    
    @Test
    void testSettersAndGetters() {
        Card card = new Card();
        CardCreationType creationType = CardCreationType.MANUAL_UPLOAD;
        
        card.setDeck(mockDeck);
        card.setQuestion(testQuestion);
        card.setAnswer(testAnswer);
        card.setCreationType(creationType);
        card.setImageUrl(testImageUrl);
        
        assertEquals(mockDeck, card.getDeck());
        assertEquals(testQuestion, card.getQuestion());
        assertEquals(testAnswer, card.getAnswer());
        assertEquals(creationType, card.getCreationType());
        assertEquals(testImageUrl, card.getImageUrl());
    }
    
    @Test
    void testCreationTypeEnumValues() {
        Card card = new Card();
        
        card.setCreationType(CardCreationType.MANUAL_UPLOAD);
        assertEquals(CardCreationType.MANUAL_UPLOAD, card.getCreationType());
        
        card.setCreationType(CardCreationType.AI_PARSED);
        assertEquals(CardCreationType.AI_PARSED, card.getCreationType());
    }
    
    @Test
    void testImageUrlIsOptional() {
        Card card = new Card(mockDeck, testQuestion, testAnswer, CardCreationType.MANUAL_UPLOAD);
        
        assertNull(card.getImageUrl());
        
        card.setImageUrl(testImageUrl);
        assertEquals(testImageUrl, card.getImageUrl());
        
        card.setImageUrl(null);
        assertNull(card.getImageUrl());
    }
}
