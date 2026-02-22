package com.study_companion.backend.controller.redis;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study_companion.backend.model.redis.CachedDecks;
import com.study_companion.backend.model.redis.DeckCache;
import com.study_companion.backend.model.redis.Session;
import com.study_companion.backend.service.SessionCacheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping(value = "/api/session")
@CrossOrigin
@Tag(name = "Session Cache", description = "Redis-based session management and deck caching")
public class SessionCacheController {

    private final SessionCacheService sessionService;

    SessionCacheController(SessionCacheService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/cachedDecks/get")
    @Operation(summary = "Get cached decks", description = "Retrieve cached decks for the authenticated user's active session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cached decks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "No active session or cached decks found")
    })
    public Optional<CachedDecks> getCachedDecks(@Parameter(hidden = true) Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());

        return sessionService.getCachedDecksIfSessionValid(currUserId);
    }

    @PostMapping("/createNewSession")
    @Operation(summary = "Create new session", description = "Create a new Redis session for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<Session> createNewSession(@Parameter(hidden = true) Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        Session session = sessionService.createSession(currUserId, null);

        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PostMapping("/cachedDecks/createCachedDecks")
    @Operation(summary = "Create cached decks", description = "Cache multiple decks for the authenticated user's session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cached decks created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<CachedDecks> createCachedDecks(
            @Parameter(description = "List of deck caches to create") @RequestBody List<DeckCache> deckCaches,
            @Parameter(hidden = true) Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        CachedDecks decks = sessionService.createOrUpdateCachedDecks(currUserId, deckCaches);

        return ResponseEntity.status(HttpStatus.CREATED).body(decks);
    }

    @PutMapping("/refreshSession")
    @Operation(summary = "Refresh session", description = "Refresh the authenticated user's session, optionally extending by additional seconds")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<Boolean> refreshSession(
            @Parameter(description = "Optional additional seconds to extend session") @RequestBody(required = false) Long additionalSeconds, 
            @Parameter(hidden = true) Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());

        Boolean isRefreshed = false;
        if (additionalSeconds == null) {
            isRefreshed = sessionService.refreshSession(currUserId);
        } else {
            isRefreshed = sessionService.refreshSession(currUserId, additionalSeconds);
        }
        
        return ResponseEntity.ok(isRefreshed);
    }

    @PutMapping("/cachedDecks/refresh")
    @Operation(summary = "Refresh cached decks", description = "Refresh cached decks with new deck data for the authenticated user's session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cached decks refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cached decks not found")
    })
    public ResponseEntity<CachedDecks> refreshCachedDecks(
            @Parameter(description = "List of deck caches to refresh with") @RequestBody List<DeckCache> deckCaches, 
            @Parameter(hidden = true) Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        CachedDecks refreshedCachedDecks = sessionService.refreshCachedDecks(currUserId, deckCaches);

        return ResponseEntity.ok(refreshedCachedDecks);
    }

    @PutMapping("/cachedDecks/update/{deckId}")
    @Operation(summary = "Update cached deck", description = "Update a specific deck in the authenticated user's cache")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cached deck updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Deck not found in cache")
    })
    public ResponseEntity<CachedDecks> updateCachedDeck(
            @Parameter(description = "UUID of the deck to update in cache") @PathVariable UUID deckId, 
            @Parameter(description = "Updated deck cache data") @RequestBody DeckCache updatedDeckCache, 
            @Parameter(hidden = true) Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        CachedDecks updatedCachedDecks = sessionService.updateSpecificDeckInCache(currUserId, deckId, updatedDeckCache);
        
        return ResponseEntity.ok(updatedCachedDecks);
    }

    @PutMapping("/cachedDecks/remove/{deckId}")
    @Operation(summary = "Remove deck from cache", description = "Remove a specific deck from the authenticated user's cache")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deck removed from cache successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Deck not found in cache")
    })
    public ResponseEntity<CachedDecks> removeDeckFromCache(
            @Parameter(description = "UUID of the deck to remove from cache") @PathVariable UUID deckId, 
            @Parameter(hidden = true) Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        CachedDecks updatedCachedDecks = sessionService.removeSingleDeckFromCache(currUserId, deckId);
        
        return ResponseEntity.ok(updatedCachedDecks);
    }

    @DeleteMapping("/logout")
    @Operation(summary = "Clear session and cached decks", description = "Clear the authenticated user's session and all cached decks (logout)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session and cached decks cleared successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<String> clearSessionAndCachedDecks(@Parameter(hidden = true) Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        sessionService.clearSessionAndCachedDecks(currUserId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cachedDecks/invalidate")
    @Operation(summary = "Invalidate cached decks", description = "Invalidate all cached decks for the authenticated user while maintaining session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cached decks invalidated successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<String> invalidateCachedDecks(@Parameter(hidden = true) Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        sessionService.invalidateCachedDecks(currUserId);
        
        return ResponseEntity.noContent().build();
    }
}
