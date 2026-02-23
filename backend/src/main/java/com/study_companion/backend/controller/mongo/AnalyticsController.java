// package com.study_companion.backend.controller.mongo;

// import java.util.List;
// import java.util.UUID;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.study_companion.backend.dto.AnalyticsDto;
// import com.study_companion.backend.exception.analytics.UnauthorizedAnalyticsAccessException;
// import com.study_companion.backend.model.mongo.DeckAnalytics;
// import com.study_companion.backend.model.mongo.ReviewSession;
// import com.study_companion.backend.model.postgres.Deck;
// import com.study_companion.backend.service.DeckAnalyticsService;
// import com.study_companion.backend.service.DeckService;
// import com.study_companion.backend.util.SecurityUtils;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.tags.Tag;

// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;

// @RestController
// @RequestMapping(value = "/api/analytics")
// @CrossOrigin
// @Tag(name = "Analytics", description = "Study session analytics and performance tracking")
// public class AnalyticsController {

//     private final DeckAnalyticsService analyticsService;

//     private final DeckService deckService;

//     private final SecurityUtils securityUtils;

//     AnalyticsController(DeckAnalyticsService analyticsService, DeckService deckService, SecurityUtils securityUtils) {
//         this.analyticsService = analyticsService;
//         this.deckService = deckService;
//         this.securityUtils = securityUtils;
//     }

//     @GetMapping("/deck/{deckId}")
//     @Operation(summary = "Fetch analytics for specific deck", description = "Retrieve performance analytics and statistics for a specific deck. Requires deck ownership or admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not deck owner or admin"),
//             @ApiResponse(responseCode = "404", description = "Deck not found")
//     })
//     public List<DeckAnalytics> getAnalyticsByDeckId(
//             @Parameter(description = "UUID of the deck to get analytics for") @PathVariable UUID deckId,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         Deck deck = deckService.getDeckById(deckId);
//         validateAccess(requestingUserId, deck.getUser().getId(), authentication);

//         return analyticsService.getAnalyticsByDeckId(deckId);
//     }

//     @GetMapping("/user/{userId}")
//     @Operation(summary = "Fetch analytics for specific user", description = "Retrieve performance analytics and statistics for a specific user. Requires logged in user to have ownership or admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not analytics owner or admin"),
//             @ApiResponse(responseCode = "404", description = "User not found")
//     })
//     public List<DeckAnalytics> getAnalyticsByUserId(
//             @Parameter(description = "UUID of the user to get analytics for") @PathVariable UUID userId,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         validateAccess(requestingUserId, userId, authentication);

//         return analyticsService.getAnalyticsByUserId(userId);
//     }

//     @GetMapping("/user/descendingOrder/{userId}")
//     @Operation(summary = "Fetch analytics for specific user in descending order", description = "Retrieve performance analytics and statistics for a specific user in descending order. Requires logged in user to have ownership or admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not analytics owner or admin"),
//             @ApiResponse(responseCode = "404", description = "User not found")
//     })
//     public List<DeckAnalytics> getAnalyticsByUserIdDescendingOrder(
//             @Parameter(description = "UUID of the user to get analytics for") @PathVariable UUID userId,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         validateAccess(requestingUserId, userId, authentication);

//         return analyticsService.getAnalyticsByUserIdOrderByScore(userId);
//     }

//     @GetMapping("/user/proficient/{userId}")
//     @Operation(summary = "Fetch analytics for specific user in which they are proficient", description = "Retrieve performance analytics and statistics for a specific user in which they are proficient. Requires logged in user to have ownership or admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not analytics owner or admin"),
//             @ApiResponse(responseCode = "404", description = "User not found")
//     })
//     public List<DeckAnalytics> getProficientDecksByUserId(
//             @Parameter(description = "UUID of the user to get analytics for") @PathVariable UUID userId,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         validateAccess(requestingUserId, userId, authentication);

//         return analyticsService.getProficientDecks(userId);
//     }

//     @GetMapping("/reviewSessions/deck/{deckId}")
//     @Operation(summary = "Fetch review sessions for specific deck", description = "Retrieve performance review sessions and statistics for a specific deck. Requires deck ownership or admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Review sessions retrieved successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not deck owner or admin"),
//             @ApiResponse(responseCode = "404", description = "Deck not found")
//     })
//     public List<ReviewSession> getReviewSessionsByDeckId(
//             @Parameter(description = "UUID of the deck to get review sessions for") @PathVariable UUID deckId,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         Deck deck = deckService.getDeckById(deckId);
//         validateAccess(requestingUserId, deck.getUser().getId(), authentication);

//         return analyticsService.getReviewSessionsByDeckId(deckId);
//     }

//     @GetMapping("/reviewSessions/user/{userId}")
//     @Operation(summary = "Fetch review sessions for specific user", description = "Retrieve performance review sessions and statistics for a specific user. Requires review session ownership or admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Review sessions retrieved successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not review sessions owner or admin"),
//             @ApiResponse(responseCode = "404", description = "User not found")
//     })
//     public List<ReviewSession> getReviewSessionsByUserId(
//             @Parameter(description = "UUID of the user to get review sessions for") @PathVariable UUID userId,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         validateAccess(requestingUserId, userId, authentication);

//         return analyticsService.getReviewSessionsByUserId(userId);
//     }

//     @GetMapping("latestReviewSession/{deckId}")
//     @Operation(summary = "Fetch latest review session for specific deck", description = "Retrieve latest performance review sessions and statistics for a specific deck. Requires deck ownership or admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Review session retrieved successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not deck owner or admin"),
//             @ApiResponse(responseCode = "404", description = "Deck not found")
//     })
//     public ReviewSession getLatestReviewSession(
//             @Parameter(description = "UUID of the deck to get latest review session for") @PathVariable UUID deckId,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         Deck deck = deckService.getDeckById(deckId);
//         validateAccess(requestingUserId, deck.getUser().getId(), authentication);

//         return analyticsService.getLatestReviewSession(deckId);
//     }

//     @PostMapping("/createNewReviewSession")
//     @Operation(summary = "Create new review session", description = "Create new review session for designated deck and user")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "201", description = "Successfully created new review session"),
//             @ApiResponse(responseCode = "400", description = "Invalid input"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not analytics owner or admin"),
//             @ApiResponse(responseCode = "404", description = "Deck not found")
//     })
//     public ResponseEntity<ReviewSession> createNewReviewSession(
//             @Parameter(description = "Creation dto containing deckId, userId, deckName, score, cardsReviewed and correctAnswers") @RequestBody AnalyticsDto.CreateReview analyticsDto,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());

//         if (!requestingUserId.equals(analyticsDto.userId())) {
//             throw new UnauthorizedAnalyticsAccessException("You can only create analytics for your own account");
//         }

//         ReviewSession newReviewSession = analyticsService.createReviewSession(analyticsDto.deckId(),
//                 analyticsDto.userId(), analyticsDto.deckName(),
//                 analyticsDto.score(), analyticsDto.cardsReviewed(), analyticsDto.correctAnswers());

//         return ResponseEntity.status(HttpStatus.CREATED).body(newReviewSession);
//     }

//     @DeleteMapping("/delete/deck/{deckId}")
//     @Operation(summary = "Delete all analytics of specific deck", description = "Delete all analytics of specific deck. Requires logged in user to have ownership or admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "204", description = "Analytics deleted successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not deck owner or admin"),
//             @ApiResponse(responseCode = "404", description = "Deck not found")
//     })
//     public ResponseEntity<String> deleteByDeckId(
//             @Parameter(description = "UUID of the deck whose analytics to delete") @PathVariable UUID deckId,
//             @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         Deck deck = deckService.getDeckById(deckId);
//         validateDeleteAccess(requestingUserId, deck.getUser().getId(), authentication);

//         analyticsService.deleteByDeckId(deckId);
//         return ResponseEntity.noContent().build();
//     }

//     @DeleteMapping("/delete/user/{userId}")
//     @Operation(summary = "Delete all analytics of specific user", description = "Delete all analytics of specific user. Requires logged in user to have ownership or admin privileges.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "204", description = "User analytics deleted successfully"),
//             @ApiResponse(responseCode = "401", description = "User not authenticated"),
//             @ApiResponse(responseCode = "403", description = "Access denied - not analytics owner or admin"),
//             @ApiResponse(responseCode = "404", description = "User not found")
//     })
//     public ResponseEntity<String> deleteByUserId(@Parameter(description = "UUID of the user whose analytics to delete") @PathVariable UUID userId, @Parameter(hidden = true) Authentication authentication) {
//         UUID requestingUserId = UUID.fromString(authentication.getName());
//         validateDeleteAccess(requestingUserId, userId, authentication);

//         analyticsService.deleteByUserId(userId);
//         return ResponseEntity.noContent().build();
//     }

//     private void validateDeleteAccess(UUID requestingUserId, UUID resourceOwnerId, Authentication auth) {
//         if (!securityUtils.isAdmin(auth) && !requestingUserId.equals(resourceOwnerId)) {
//             throw new UnauthorizedAnalyticsAccessException("Unauthorized deleting of analytics");
//         }
//     }

//     private void validateAccess(UUID requestingUserId, UUID resourceOwnerId, Authentication auth) {
//         if (!securityUtils.isAdmin(auth) && !requestingUserId.equals(resourceOwnerId)) {
//             throw new UnauthorizedAnalyticsAccessException("Unauthorized analytics access");
//         }
//     }
// }
