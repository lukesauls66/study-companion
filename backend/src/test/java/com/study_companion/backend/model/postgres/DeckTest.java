package com.study_companion.backend.model.postgres;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DeckTest {

    @Mock
    private User mockUser;

    private String title;
    private String description;

    @BeforeEach
    void setUp() {
        title = "Sample Deck";
        description = "This is a sample deck description.";
    }

    @Test
    void testDefaultConstructor() {
        Deck deck = new Deck();
        assertNotNull(deck);
        assertNull(deck.getUser());
        assertNull(deck.getTitle());
        assertNull(deck.getDescription());
        assertNotNull(deck.getCards());
        assertTrue(deck.getCards().isEmpty());
        assertNotNull(deck.getUploads());
        assertTrue(deck.getUploads().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        Deck deck = new Deck(mockUser, title, description);
        assertNotNull(deck);
        assertEquals(mockUser, deck.getUser());
        assertEquals(title, deck.getTitle());
        assertEquals(description, deck.getDescription());
        assertNotNull(deck.getCards());
        assertTrue(deck.getCards().isEmpty());
        assertNotNull(deck.getUploads());
        assertTrue(deck.getUploads().isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        Deck deck = new Deck();

        deck.setUser(mockUser);
        deck.setTitle(title);
        deck.setDescription(description);

        assertEquals(mockUser, deck.getUser());
        assertEquals(title, deck.getTitle());
        assertEquals(description, deck.getDescription());
    }

    @Test
    void testAddCard() {
        Deck deck = new Deck();
        Card card = new Card();

        deck.addCard(card);

        assertEquals(1, deck.getCards().size());
        assertEquals(card, deck.getCards().get(0));

        assertEquals(deck, card.getDeck());
    }

    @Test
    void testRemoveCard() {
        Deck deck = new Deck();
        Card card = new Card();
        deck.addCard(card);

        deck.removeCard(card);

        assertEquals(0, deck.getCards().size());
        assertNull(card.getDeck());
    }

    @Test
    void testAddUpload() {
        Deck deck = new Deck();
        Upload upload = new Upload();

        deck.addUpload(upload);

        assertEquals(1, deck.getUploads().size());
        assertEquals(upload, deck.getUploads().get(0));

        assertEquals(deck, upload.getDeck());
    }

    @Test
    void testRemoveUpload() {
        Deck deck = new Deck();
        Upload upload = new Upload();
        deck.addUpload(upload);

        deck.removeUpload(upload);

        assertEquals(0, deck.getUploads().size());
        assertNull(upload.getDeck());
    }
}
