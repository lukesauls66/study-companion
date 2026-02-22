package com.study_companion.backend.controller.postgres;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study_companion.backend.dto.DeckDto;
import com.study_companion.backend.model.postgres.Deck;
import com.study_companion.backend.service.DeckService;

@RestController
@RequestMapping(value = "/api/deck")
@CrossOrigin
public class DeckController {

    private final DeckService deckService;

    DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    // FIXME: POST 201 with return
    // ResponseEntity.status(HttpStatus.CREATED).body(createdUser), PUT 200 with
    // return ResponseEntity.ok(updatedDeck), DELETE 204 with return
    // ResponseEntity.noContent().build()

    @GetMapping("/getAllDecks")
    public List<Deck> getAllDecks() {
        return deckService.getAllDecks();
    }

    @GetMapping("/{deckId}")
    public Deck getDeckById(@PathVariable UUID deckId) {
        return deckService.getDeckById(deckId);
    }

    @GetMapping("/user/{userId}")
    public List<Deck> getAllUserDecks(@PathVariable UUID userId) {
        return deckService.getAllUserDecks(userId);
    }

    @GetMapping("/user/{userId}/count")
    public long countUserDecks(@PathVariable UUID userId) {
        return deckService.getCountOfAllUserDecks(userId);
    }

    @PostMapping("/createDeck")
    public ResponseEntity<String> postMethodName(@RequestBody DeckDto.Create deckDto) {
        deckService.createDeck(deckDto);
        return ResponseEntity.ok("Successfully created new deck");
    }

    @PutMapping("/update/{deckId}")
    public ResponseEntity<String> updateDeck(@PathVariable UUID deckId, @RequestBody DeckDto.Update deckDto,
            Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        deckService.updateDeck(deckId, deckDto, requestingUserId);
        return ResponseEntity.ok("Successfully updated deck");
    }

    @DeleteMapping("/delete/{deckId}")
    public ResponseEntity<String> deleteDeck(@PathVariable UUID deckId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        deckService.deleteDeckById(deckId, requestingUserId);
        return ResponseEntity.ok("Successfully deleted deck");
    }

    @DeleteMapping("/delete/user/{userId}")
    public ResponseEntity<String> deleteUserDecks(@PathVariable UUID userId) {
        deckService.deleteAllUserDecks(userId);
        return ResponseEntity.ok("Successfully deleted all user's decks");
    }
}
