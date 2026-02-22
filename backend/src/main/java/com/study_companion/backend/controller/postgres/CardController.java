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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study_companion.backend.dto.CardDto;
import com.study_companion.backend.model.CardCreationType;
import com.study_companion.backend.model.postgres.Card;
import com.study_companion.backend.service.CardService;

@RestController
@RequestMapping(value = "/api/card")
@CrossOrigin
public class CardController {

    private final CardService cardService;

    CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // FIXME: POST 201 with return
    // ResponseEntity.status(HttpStatus.CREATED).body(createdUser), PUT 200 with
    // return ResponseEntity.ok(updatedDeck), DELETE 204 with return
    // ResponseEntity.noContent().build()

    @GetMapping("/getCards")
    public List<Card> getAllCards() {
        return cardService.getAllCards();
    }

    @GetMapping("/{cardId}")
    public Card getCardById(@PathVariable UUID cardId) {
        return cardService.getCardById(cardId);
    }

    @GetMapping("/deck/{deckId}")
    public List<Card> getCardsFromDeck(@PathVariable UUID deckId) {
        return cardService.getAllDeckCards(deckId);
    }

    @GetMapping("/deck/{deckId}/count")
    public long countDeckCards(@PathVariable UUID deckId) {
        return cardService.getCountOfAllDeckCards(deckId);
    }

    @PostMapping("/createCard")
    public ResponseEntity<String> createNewCard(@RequestBody CardDto.Create cardDto,
            @RequestParam CardCreationType cardCreationType, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        cardService.createCard(cardDto, cardCreationType, requestingUserId);
        return ResponseEntity.ok("Successfully created new card");
    }

    @PutMapping("/update/{cardId}")
    public ResponseEntity<String> updateCard(@PathVariable UUID cardId, @RequestBody CardDto.Update cardDto,
            Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        cardService.updateCard(cardId, cardDto, requestingUserId);
        return ResponseEntity.ok("Successfully updated card");
    }

    @DeleteMapping("/delete/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable UUID cardId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        cardService.deleteCardById(cardId, requestingUserId);
        return ResponseEntity.ok("Successfully deleted card");
    }

    @DeleteMapping("/delete/deck/{deckId}")
    public ResponseEntity<String> deleteDeckCards(@PathVariable UUID deckId) {
        cardService.deleteAllDeckCards(deckId);
        return ResponseEntity.ok("Successfully deleted all deck's cards");
    }
}
