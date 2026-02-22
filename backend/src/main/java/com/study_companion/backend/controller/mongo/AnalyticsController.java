package com.study_companion.backend.controller.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study_companion.backend.dto.AnalyticsDto;
import com.study_companion.backend.exception.analytics.UnauthorizedAnalyticsAccessException;
import com.study_companion.backend.model.mongo.DeckAnalytics;
import com.study_companion.backend.model.mongo.ReviewSession;
import com.study_companion.backend.model.postgres.Deck;
import com.study_companion.backend.service.DeckAnalyticsService;
import com.study_companion.backend.service.DeckService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(value = "/api/analytics")
@CrossOrigin
public class AnalyticsController {

    private final DeckAnalyticsService analyticsService;

    private final DeckService deckService;

    AnalyticsController(DeckAnalyticsService analyticsService, DeckService deckService) {
        this.analyticsService = analyticsService;
        this.deckService = deckService;
    }

    // FIXME: POST 201 with return
    // ResponseEntity.status(HttpStatus.CREATED).body(createdUser), PUT 200 with
    // return ResponseEntity.ok(updatedDeck), DELETE 204 with return
    // ResponseEntity.noContent().build()

    @GetMapping("/deck/{deckId}")
    public List<DeckAnalytics> getAnalyticsByDeckId(@PathVariable UUID deckId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        Deck deck = deckService.getDeckById(deckId);
        validateAccess(requestingUserId, deck.getUser().getId(), authentication);

        return analyticsService.getAnalyticsByDeckId(deckId);
    }

    @GetMapping("/user/{userId}")
    public List<DeckAnalytics> getAnalyticsByUserId(@PathVariable UUID userId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateAccess(requestingUserId, userId, authentication);

        return analyticsService.getAnalyticsByUserId(userId);
    }

    @GetMapping("/user/descendingOrder/{userId}")
    public List<DeckAnalytics> getAnalyticsByUserIdDescendingOrder(@PathVariable UUID userId,
            Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateAccess(requestingUserId, userId, authentication);

        return analyticsService.getAnalyticsByUserIdOrderByScore(userId);
    }

    @GetMapping("/user/proficient/{userId}")
    public List<DeckAnalytics> getProficientDecksByUserId(@PathVariable UUID userId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateAccess(requestingUserId, userId, authentication);

        return analyticsService.getProficientDecks(userId);
    }

    @GetMapping("/reviewSessions/deck/{deckId}")
    public List<ReviewSession> getReviewSessionsByDeckId(@PathVariable UUID deckId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        Deck deck = deckService.getDeckById(deckId);
        validateAccess(requestingUserId, deck.getUser().getId(), authentication);

        return analyticsService.getReviewSessionsByDeckId(deckId);
    }

    @GetMapping("/reviewSessions/user/{userId}")
    public List<ReviewSession> getReviewSessionsByUserId(@PathVariable UUID userId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateAccess(requestingUserId, userId, authentication);

        return analyticsService.getReviewSessionsByUserId(userId);
    }

    @GetMapping("latestReviewSession/{deckId}")
    public ReviewSession getLatestReviewSession(@PathVariable UUID deckId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        Deck deck = deckService.getDeckById(deckId);
        validateAccess(requestingUserId, deck.getUser().getId(), authentication);

        return analyticsService.getLatestReviewSession(deckId);
    }

    @PostMapping("/createNewReviewSession")
    public ResponseEntity<String> createNewReviewSession(@RequestBody AnalyticsDto.CreateReview analyticsDto,
            Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());

        if (!requestingUserId.equals(analyticsDto.userId())) {
            throw new UnauthorizedAnalyticsAccessException("You can only create analytics for your own account");
        }

        analyticsService.createReviewSession(analyticsDto.deckId(), analyticsDto.userId(), analyticsDto.deckName(),
                analyticsDto.score(), analyticsDto.cardsReviewed(), analyticsDto.correctAnswers());

        return ResponseEntity.ok("Successfully created new review session");
    }

    @DeleteMapping("/delete/deck/{deckId}")
    public ResponseEntity<String> deleteByDeckId(@PathVariable UUID deckId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        Deck deck = deckService.getDeckById(deckId);
        validateDeleteAccess(requestingUserId, deck.getUser().getId(), authentication);

        analyticsService.deleteByDeckId(deckId);
        return ResponseEntity.ok("Successfully deleted review sessions and analytics associated with deckId");
    }

    @DeleteMapping("/delete/user/{userId}")
    public ResponseEntity<String> deleteByUserId(@PathVariable UUID userId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        validateDeleteAccess(requestingUserId, userId, authentication);

        analyticsService.deleteByUserId(userId);
        return ResponseEntity.ok("Successfully deleted review sessions and analytics associated with userId");
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private void validateDeleteAccess(UUID requestingUserId, UUID resourceOwnerId, Authentication auth) {
        if (!isAdmin(auth) && !requestingUserId.equals(resourceOwnerId)) {
            throw new UnauthorizedAnalyticsAccessException("Unauthorized deleting of analytics");
        }
    }

    private void validateAccess(UUID requestingUserId, UUID resourceOwnerId, Authentication auth) {
        if (!isAdmin(auth) && !requestingUserId.equals(resourceOwnerId)) {
            throw new UnauthorizedAnalyticsAccessException("Unauthorized analytics access");
        }
    }
}
