package com.study_companion.backend.controller.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study_companion.backend.dto.AnalyticsDto;
import com.study_companion.backend.dto.DeckDto;
import com.study_companion.backend.dto.GenericDto;
import com.study_companion.backend.exception.analytics.UnauthorizedAnalyticsAccessException;
import com.study_companion.backend.model.mongo.DeckAnalytics;
import com.study_companion.backend.model.mongo.ReviewSession;
import com.study_companion.backend.service.DeckAnalyticsService;
import com.study_companion.backend.service.DeckService;
import com.study_companion.backend.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(value = "/api/analytics")
@CrossOrigin
@Tag(name = "Analytics", description = "Study session analytics and performance tracking")
public class AnalyticsController {

    private final DeckAnalyticsService analyticsService;

    private final DeckService deckService;

    private final SecurityUtils securityUtils;

    AnalyticsController(DeckAnalyticsService analyticsService, DeckService deckService, SecurityUtils securityUtils) {
        this.analyticsService = analyticsService;
        this.deckService = deckService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/deck/{deckId}")
    @Operation(summary = "Fetch analytics for specific deck", description = "Retrieve performance analytics and statistics for a specific deck. Requires deck ownership or admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeckAnalytics.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
    })
    public ResponseEntity<List<DeckAnalytics>> getAnalyticsByDeckId(
            @Parameter(description = "UUID of the deck to get analytics for") @PathVariable UUID deckId,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        DeckDto.GetResponse deck = deckService.getDeckById(deckId);
        validateAccess(requestingUserId, deck.userId(), authentication);

        List<DeckAnalytics> analytics = analyticsService.getAnalyticsByDeckId(deckId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Fetch analytics for specific user", description = "Retrieve performance analytics and statistics for a specific user. Requires logged in user to have ownership or admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeckAnalytics.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"User not found\", \"error\": true }")))
    })
    public ResponseEntity<List<DeckAnalytics>> getAnalyticsByUserId(
            @Parameter(description = "UUID of the user to get analytics for") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateAccess(requestingUserId, userId, authentication);

        List<DeckAnalytics> analytics = analyticsService.getAnalyticsByUserId(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/user/descendingOrder/{userId}")
    @Operation(summary = "Fetch analytics for specific user in descending order", description = "Retrieve performance analytics and statistics for a specific user in descending order. Requires logged in user to have ownership or admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeckAnalytics.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"User not found\", \"error\": true }")))
    })
    public ResponseEntity<List<DeckAnalytics>> getAnalyticsByUserIdDescendingOrder(
            @Parameter(description = "UUID of the user to get analytics for") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateAccess(requestingUserId, userId, authentication);

        List<DeckAnalytics> analytics = analyticsService.getAnalyticsByUserIdOrderByScore(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/user/proficient/{userId}")
    @Operation(summary = "Fetch analytics for specific user in which they are proficient", description = "Retrieve performance analytics and statistics for a specific user in which they are proficient. Requires logged in user to have ownership or admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeckAnalytics.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"User not found\", \"error\": true }")))
    })
    public ResponseEntity<List<DeckAnalytics>> getProficientDecksByUserId(
            @Parameter(description = "UUID of the user to get analytics for") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateAccess(requestingUserId, userId, authentication);

        List<DeckAnalytics> analytics = analyticsService.getProficientDecks(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/reviewSessions/deck/{deckId}")
    @Operation(summary = "Fetch review sessions for specific deck", description = "Retrieve performance review sessions and statistics for a specific deck. Requires deck ownership or admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review sessions retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewSession.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
    })
    public ResponseEntity<List<ReviewSession>> getReviewSessionsByDeckId(
            @Parameter(description = "UUID of the deck to get review sessions for") @PathVariable UUID deckId,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        DeckDto.GetResponse deck = deckService.getDeckById(deckId);
        validateAccess(requestingUserId, deck.userId(), authentication);

        List<ReviewSession> reviewSessions = analyticsService.getReviewSessionsByDeckId(deckId);
        return ResponseEntity.ok(reviewSessions);
    }

    @GetMapping("/reviewSessions/user/{userId}")
    @Operation(summary = "Fetch review sessions for specific user", description = "Retrieve performance review sessions and statistics for a specific user. Requires review session ownership or admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review sessions retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewSession.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"User not found\", \"error\": true }")))
    })
    public ResponseEntity<List<ReviewSession>> getReviewSessionsByUserId(
            @Parameter(description = "UUID of the user to get review sessions for") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateAccess(requestingUserId, userId, authentication);

        List<ReviewSession> reviewSessions = analyticsService.getReviewSessionsByUserId(userId);
        return ResponseEntity.ok(reviewSessions);
    }

    @GetMapping("latestReviewSession/{deckId}")
    @Operation(summary = "Fetch latest review session for specific deck", description = "Retrieve latest performance review sessions and statistics for a specific deck. Requires deck ownership or admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review session retrieved successfully", content = @Content(schema = @Schema(implementation = ReviewSession.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
    })
    public ResponseEntity<ReviewSession> getLatestReviewSession(
            @Parameter(description = "UUID of the deck to get latest review session for") @PathVariable UUID deckId,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        DeckDto.GetResponse deck = deckService.getDeckById(deckId);
        validateAccess(requestingUserId, deck.userId(), authentication);

        ReviewSession reviewSession = analyticsService.getLatestReviewSession(deckId);
        return ResponseEntity.ok(reviewSession);
    }

    @PostMapping("/createNewReviewSession")
    @Operation(summary = "Create new review session", description = "Create new review session for designated deck and user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created new review session", content = @Content(schema = @Schema(implementation = ReviewSession.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameter", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid parameter\", \"error\": true }"))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
    })
    public ResponseEntity<ReviewSession> createNewReviewSession(
            @Parameter(description = "Creation dto containing deckId, userId, deckName, score, cardsReviewed and correctAnswers") @RequestBody AnalyticsDto.CreateReview analyticsDto,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());

        if (!requestingUserId.equals(analyticsDto.userId())) {
            throw new UnauthorizedAnalyticsAccessException("You can only create analytics for your own account");
        }

        ReviewSession newReviewSession = analyticsService.createReviewSession(analyticsDto.deckId(),
                analyticsDto.userId(), analyticsDto.deckName(),
                analyticsDto.score(), analyticsDto.cardsReviewed(), analyticsDto.correctAnswers());

        return ResponseEntity.status(HttpStatus.CREATED).body(newReviewSession);
    }

    @DeleteMapping("/delete/deck/{deckId}")
    @Operation(summary = "Delete all analytics of specific deck", description = "Delete all analytics of specific deck. Requires logged in user to have ownership or admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Analytics deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "Deck not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Deck not found\", \"error\": true }")))
    })
    public ResponseEntity<String> deleteByDeckId(
            @Parameter(description = "UUID of the deck whose analytics to delete") @PathVariable UUID deckId,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        DeckDto.GetResponse deck = deckService.getDeckById(deckId);
        validateDeleteAccess(requestingUserId, deck.userId(), authentication);

        analyticsService.deleteByDeckId(deckId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/user/{userId}")
    @Operation(summary = "Delete all analytics of specific user", description = "Delete all analytics of specific user. Requires logged in user to have ownership or admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User analytics deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Access denied\", \"error\": true }"))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericDto.ErrorResponse.class), examples = @ExampleObject(value = "{ \"message\": \"User not found\", \"error\": true }")))
    })
    public ResponseEntity<String> deleteByUserId(
            @Parameter(description = "UUID of the user whose analytics to delete") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateDeleteAccess(requestingUserId, userId, authentication);

        analyticsService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    private void validateDeleteAccess(UUID requestingUserId, UUID resourceOwnerId, Authentication auth) {
        if (!securityUtils.isAdmin(auth) && !requestingUserId.equals(resourceOwnerId)) {
            throw new UnauthorizedAnalyticsAccessException("Unauthorized deleting of analytics");
        }
    }

    private void validateAccess(UUID requestingUserId, UUID resourceOwnerId, Authentication auth) {
        if (!securityUtils.isAdmin(auth) && !requestingUserId.equals(resourceOwnerId)) {
            throw new UnauthorizedAnalyticsAccessException("Unauthorized analytics access");
        }
    }
}
