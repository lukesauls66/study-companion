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
import org.springframework.web.bind.annotation.RestController;

import com.study_companion.backend.dto.DeckDto;
import com.study_companion.backend.dto.GenericDto;
import com.study_companion.backend.service.DeckService;

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
@RequestMapping(value = "/api/deck")
@CrossOrigin
@Tag(name = "Deck Management", description = "Operations for managing flashcard decks")
public class DeckController {

        private final DeckService deckService;

        DeckController(DeckService deckService) {
                this.deckService = deckService;
        }

        @GetMapping("/getAllDecks")
        @Operation(summary = "Get all decks", description = "Retrieve all flashcard decks in the system. Requires authentication.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Decks retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeckDto.GetResponse.class)))),
                        @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }")))
        })
        public ResponseEntity<List<DeckDto.GetResponse>> getAllDecks() {
                List<DeckDto.GetResponse> decks = deckService.getAllDecks();
                return ResponseEntity.ok(decks);
        }

        @GetMapping("/{deckId}")
        @Operation(summary = "Get deck by ID", description = "Retrieve a specific flashcard deck by its UUID. Requires authentication.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Deck retrieved successfully", content = @Content(schema = @Schema(implementation = DeckDto.GetResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid deck parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
        })
        public ResponseEntity<DeckDto.GetResponse> getDeckById(
                        @Parameter(description = "UUID of the deck to retrieve") @PathVariable UUID deckId) {
                DeckDto.GetResponse deck = deckService.getDeckById(deckId);
                return ResponseEntity.ok(deck);
        }

        @GetMapping("/{deckId}/withCardsAndUploads")
        @Operation(summary = "Get deck by ID", description = "Retrieve a specific flashcard deck by its UUID. Requires authentication.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Deck retrieved successfully", content = @Content(schema = @Schema(implementation = DeckDto.GetResponseWithCardsAndUploads.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid deck parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
        })
        public ResponseEntity<DeckDto.GetResponseWithCardsAndUploads> getDeckByIdWithCards(
                        @Parameter(description = "UUID of the deck to retrieve") @PathVariable UUID deckId) {
                DeckDto.GetResponseWithCardsAndUploads deck = deckService.getDeckByIdWithCardsAndUploads(deckId);
                return ResponseEntity.ok(deck);
        }

        @GetMapping("/user/{userId}")
        @Operation(summary = "Get user's decks", description = "Retrieve all flashcard decks belonging to a specific user. Requires authentication.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User decks retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeckDto.GetResponse.class)))),
                        @ApiResponse(responseCode = "400", description = "Invalid deck parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"User not found\", \"error\": true }")))
        })
        public ResponseEntity<List<DeckDto.GetResponse>> getAllUserDecks(
                        @Parameter(description = "UUID of the user whose decks to retrieve") @PathVariable UUID userId) {
                return ResponseEntity.ok(deckService.getAllUserDecks(userId));
        }

        @GetMapping("/user/{userId}/count")
        @Operation(summary = "Count user's decks", description = "Get the total number of decks belonging to a specific user. Requires authentication.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Deck count retrieved successfully", content = @Content(schema = @Schema(implementation = Long.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid deck parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"User not found\", \"error\": true }")))
        })
        public ResponseEntity<Long> countUserDecks(
                        @Parameter(description = "UUID of the user whose deck count to retrieve") @PathVariable UUID userId) {
                return ResponseEntity.ok(deckService.getCountOfAllUserDecks(userId));
        }

        @PostMapping("/createDeck")
        @Operation(summary = "Create new deck", description = "Create a new flashcard deck with the provided details. Requires authentication.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Deck created successfully", content = @Content(schema = @Schema(implementation = DeckDto.GetResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }")))
        })
        public ResponseEntity<DeckDto.GetResponse> createDeck(
                        @Parameter(description = "Deck creation data containing name, description, and subject") @RequestBody DeckDto.CreateRequest deckDto) {
                DeckDto.GetResponse newDeck = deckService.createDeck(deckDto);
                return ResponseEntity.status(HttpStatus.CREATED).body(newDeck);
        }

        @PutMapping("/update/{deckId}")
        @Operation(summary = "Update deck", description = "Update an existing deck's details. Only the deck owner can perform this operation.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Deck updated successfully", content = @Content(schema = @Schema(implementation = DeckDto.GetResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
        })
        public ResponseEntity<DeckDto.GetResponse> updateDeck(
                        @Parameter(description = "UUID of the deck to update") @PathVariable UUID deckId,
                        @Parameter(description = "Deck update data containing optional name, description, and subject") @RequestBody DeckDto.UpdateRequest deckDto,
                        @Parameter(hidden = true) Authentication authentication) {
                UUID requestingUserId = UUID.fromString(authentication.getName());
                DeckDto.GetResponse updatedDeck = deckService.updateDeck(deckId, deckDto, requestingUserId);
                return ResponseEntity.ok(updatedDeck);
        }

        @DeleteMapping("/delete/{deckId}")
        @Operation(summary = "Delete deck", description = "Permanently delete a deck and all associated cards. Only the deck owner can perform this operation.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Deck deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid deck parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
        })
        public ResponseEntity<String> deleteDeck(
                        @Parameter(description = "UUID of the deck to delete") @PathVariable UUID deckId,
                        @Parameter(hidden = true) Authentication authentication) {
                UUID requestingUserId = UUID.fromString(authentication.getName());
                deckService.deleteDeckById(deckId, requestingUserId);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/delete/user/{userId}")
        @Operation(summary = "Delete all user decks", description = "Delete all decks belonging to a specific user. Requires admin privileges or ownership.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "User decks deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
                        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"User not found\", \"error\": true }")))
        })
        public ResponseEntity<String> deleteUserDecks(
                        @Parameter(description = "UUID of the user whose decks to delete") @PathVariable UUID userId) {
                deckService.deleteAllUserDecks(userId);
                return ResponseEntity.noContent().build();
        }
}
