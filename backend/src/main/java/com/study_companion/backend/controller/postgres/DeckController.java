// package com.study_companion.backend.controller.postgres;

// import java.util.List;
// import java.util.UUID;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.study_companion.backend.dto.DeckDto;
// import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
// import com.study_companion.backend.exception.user.UnauthorizedUserAccessException;
// import com.study_companion.backend.model.postgres.Deck;
// import com.study_companion.backend.service.DeckService;
// import com.study_companion.backend.util.SecurityUtils;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.tags.Tag;

// @RestController
// @RequestMapping(value = "/api/deck")
// @CrossOrigin
// @Tag(name = "Deck Management", description = "Operations for managing flashcard decks")
// public class DeckController {

//     private final DeckService deckService;

//     private final SecurityUtils securityUtils;

//     DeckController(DeckService deckService, SecurityUtils securityUtils) {
//         this.deckService = deckService;
//         this.securityUtils = securityUtils;
//     }

//     @GetMapping("/getAllDecks")
//     @Operation(summary = "Get all decks", description = "Retrieve all flashcard decks in the system. Requires authentication.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Decks retrieved successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated")
//     })
//     public List<Deck> getAllDecks() {
//         return deckService.getAllDecks();
//     }

//     @GetMapping("/{deckId}")
//     @Operation(summary = "Get deck by ID", description = "Retrieve a specific flashcard deck by its UUID. Requires authentication.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Deck retrieved successfully"),
//             @ApiResponse(responseCode = "400", description = "Invalid deck ID format"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "404", description = "Deck not found")
//     })
//     public Deck getDeckById(
//             @Parameter(description = "UUID of the deck to retrieve") @PathVariable UUID deckId) {
//         return deckService.getDeckById(deckId);
//     }

//     @GetMapping("/user/{userId}")
//     @Operation(summary = "Get user's decks", description = "Retrieve all flashcard decks belonging to a specific user. Requires authentication.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "User decks retrieved successfully"),
//             @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "404", description = "User not found")
//     })
//     public List<Deck> getAllUserDecks(
//             @Parameter(description = "UUID of the user whose decks to retrieve") @PathVariable UUID userId,
//             @Parameter(hidden = true) Authentication authentication) {
//         if (!securityUtils.canAccess(authentication, userId)) {
//             throw new UnauthorizedDeckAccessException("Unauthorized deck access");
//         }
//         return deckService.getAllUserDecks(userId);
//     }

//     @GetMapping("/user/{userId}/count")
//     @Operation(summary = "Count user's decks", description = "Get the total number of decks belonging to a specific user. Requires authentication.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Deck count retrieved successfully"),
//             @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "404", description = "User not found")
//     })
//     public long countUserDecks(
//             @Parameter(description = "UUID of the user whose deck count to retrieve") @PathVariable UUID userId,
//             @Parameter(hidden = true) Authentication authentication) {
//         if (!securityUtils.canAccess(authentication, userId)) {
//             throw new UnauthorizedDeckAccessException("Unauthorized deck access");
//         }
//         return deckService.getCountOfAllUserDecks(userId);
//     }

//     @PostMapping("/createDeck")
//     @Operation(summary = "Create new deck", description = "Create a new flashcard deck with the provided details. Requires authentication.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "201", description = "Deck created successfully"),
//             @ApiResponse(responseCode = "400", description = "Invalid input data"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated")
//     })
//     public ResponseEntity<Deck> createDeck(
//             @Parameter(description = "Deck creation data containing name, description, and subject") @RequestBody DeckDto.Create deckDto) {
//         Deck newDeck = deckService.createDeck(deckDto);
//         return ResponseEntity.status(HttpStatus.CREATED).body(newDeck);
//     }

//     @PutMapping("/update/{deckId}")
//     @Operation(summary = "Update deck", description = "Update an existing deck's details. Only the deck owner can perform this operation.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Deck updated successfully"),
//             @ApiResponse(responseCode = "400", description = "Invalid input data or deck ID format"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not deck owner"),
//             @ApiResponse(responseCode = "404", description = "Deck not found")
//     })
//     public ResponseEntity<Deck> updateDeck(
//             @Parameter(description = "UUID of the deck to update") @PathVariable UUID deckId,
//             @Parameter(description = "Deck update data containing optional name, description, and subject") @RequestBody DeckDto.Update deckDto,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         Deck updatedDeck = deckService.updateDeck(deckId, deckDto, requestingUserId);
//         return ResponseEntity.ok(updatedDeck);
//     }

//     @DeleteMapping("/delete/{deckId}")
//     @Operation(summary = "Delete deck", description = "Permanently delete a deck and all associated cards. Only the deck owner can perform this operation.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "204", description = "Deck deleted successfully"),
//             @ApiResponse(responseCode = "400", description = "Invalid deck ID format"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not deck owner"),
//             @ApiResponse(responseCode = "404", description = "Deck not found")
//     })
//     public ResponseEntity<String> deleteDeck(
//             @Parameter(description = "UUID of the deck to delete") @PathVariable UUID deckId,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         deckService.deleteDeckById(deckId, requestingUserId);
//         return ResponseEntity.noContent().build();
//     }

//     @DeleteMapping("/delete/user/{userId}")
//     @Operation(summary = "Delete all user decks", description = "Delete all decks belonging to a specific user. Requires admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "204", description = "User decks deleted successfully"),
//             @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required"),
//             @ApiResponse(responseCode = "404", description = "User not found")
//     })
//     public ResponseEntity<String> deleteUserDecks(
//             @Parameter(description = "UUID of the user whose decks to delete") @PathVariable UUID userId,
//             @Parameter(hidden = true) Authentication authentication) {
//         if (!securityUtils.isAdmin(authentication)) {
//             throw new UnauthorizedUserAccessException("Unauthorized user access");
//         }
//         deckService.deleteAllUserDecks(userId);
//         return ResponseEntity.noContent().build();
//     }
// }
