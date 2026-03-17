package com.study_companion.backend.controller.postgres;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
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
import com.study_companion.backend.dto.GenericDto;
import com.study_companion.backend.model.CardCreationType;
import com.study_companion.backend.service.CardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/card")
@CrossOrigin
@Tag(name = "Card Management", description = "Operations for managing individual flashcards within decks")
public class CardController {

        private final CardService cardService;

        CardController(CardService cardService) {
                this.cardService = cardService;
        }

        @GetMapping("/getCards")
        @Operation(summary = "Get all cards", description = "Retrieve all flashcards in the system. Requires admin access.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cards retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardDto.GetResponse.class)))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }")))
        })
        public ResponseEntity<List<CardDto.GetResponse>> getAllCards() {
                List<CardDto.GetResponse> cards = cardService.getAllCards();
                return ResponseEntity.ok(cards);
        }

        @GetMapping("/{cardId}")
        @Operation(summary = "Get card by ID", description = "Retrieve a specific flashcard by its UUID. Requires authentication or admin access.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card retrieved successfully", content = @Content(schema = @Schema(implementation = CardDto.GetResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Card not found\", \"error\": true }")))
        })
        public ResponseEntity<CardDto.GetResponse> getCardById(
                        @Parameter(description = "UUID of the card to retrieve") @PathVariable UUID cardId) {
                CardDto.GetResponse card = cardService.getCardById(cardId);
                return ResponseEntity.ok(card);
        }

        @GetMapping("/deck/{deckId}")
        @Operation(summary = "Get cards from deck", description = "Retrieve all flashcards belonging to a specific deck. Requires authentication or admin access.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Deck cards retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardDto.GetResponse.class)))),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
        })
        public ResponseEntity<List<CardDto.GetResponse>> getCardsFromDeck(
                        @Parameter(description = "UUID of the deck to get cards from") @PathVariable UUID deckId) {
                List<CardDto.GetResponse> cards = cardService.getAllDeckCards(deckId);
                return ResponseEntity.ok(cards);
        }

        @GetMapping("/deck/{deckId}/count")
        @Operation(summary = "Count cards in deck", description = "Get the total number of cards in a specific deck. Requires authentication.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card count retrieved successfully", content = @Content(schema = @Schema(implementation = Long.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
        })
        public ResponseEntity<Long> countDeckCards(
                        @Parameter(description = "UUID of the deck to count cards for") @PathVariable UUID deckId) {
                return ResponseEntity.ok(cardService.getCountOfAllDeckCards(deckId));
        }

        @PostMapping("/createCard")
        @Operation(summary = "Create new card", description = "Create a new flashcard in a deck with specified creation type. Only deck owners can add cards.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Card created successfully", content = @Content(schema = @Schema(implementation = CardDto.GetResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
        })
        public ResponseEntity<CardDto.GetResponse> createNewCard(
                        @Parameter(description = "Card creation data containing question, answer, and deck ID") @RequestBody CardDto.CreateRequest cardDto,
                        @Parameter(description = "Type of card creation (MANUAL, GENERATED, etc.)") @RequestParam CardCreationType cardCreationType,
                        @Parameter(hidden = true) Authentication authentication) {
                UUID requestingUserId = UUID.fromString(authentication.getName());
                CardDto.GetResponse newCard = cardService.createCard(cardDto, cardCreationType, requestingUserId);
                return ResponseEntity.status(HttpStatus.CREATED).body(newCard);
        }

        @PutMapping("/update/{cardId}")
        @Operation(summary = "Update card", description = "Update an existing flashcard's content. Only the deck owner can perform this operation.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card updated successfully", content = @Content(schema = @Schema(implementation = CardDto.GetResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Card not found\", \"error\": true }")))
        })
        public ResponseEntity<CardDto.GetResponse> updateCard(
                        @Parameter(description = "UUID of the card to update") @PathVariable UUID cardId,
                        @Parameter(description = "Card update data containing optional question and answer") @RequestBody CardDto.UpdateRequest cardDto,
                        @Parameter(hidden = true) Authentication authentication) {
                UUID requestingUserId = UUID.fromString(authentication.getName());
                CardDto.GetResponse updatedCard = cardService.updateCard(cardId, cardDto, requestingUserId);
                return ResponseEntity.ok(updatedCard);
        }

        @DeleteMapping("/delete/{cardId}")
        @Operation(summary = "Delete card", description = "Permanently delete a flashcard. Only the deck owner can perform this operation.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Card deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Card not found\", \"error\": true }")))
        })
        public ResponseEntity<String> deleteCard(
                        @Parameter(description = "UUID of the card to delete") @PathVariable UUID cardId,
                        @Parameter(hidden = true) Authentication authentication) {
                UUID requestingUserId = UUID.fromString(authentication.getName());
                cardService.deleteCardById(cardId, requestingUserId);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/delete/deck/{deckId}")
        @Operation(summary = "Delete all deck cards", description = "Delete all cards belonging to a specific deck. Requires admin privileges.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Deck cards deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
        })
        public ResponseEntity<String> deleteDeckCards(
                        @Parameter(description = "UUID of the deck whose cards to delete") @PathVariable UUID deckId) {
                cardService.deleteAllDeckCards(deckId);
                return ResponseEntity.noContent().build();
        }
}
