package com.study_companion.backend.controller.redis;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping(value = "/api/session")
@CrossOrigin
public class SessionCacheController {

    private final SessionCacheService sessionService;

    SessionCacheController(SessionCacheService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/cachedDecks/get")
    public Optional<CachedDecks> getCachedDecks(Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());

        return sessionService.getCachedDecksIfSessionValid(currUserId);
    }

    @PostMapping("/createNewSession")
    public ResponseEntity<Session> createNewSession(Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        Session session = sessionService.createSession(currUserId, null);

        return ResponseEntity.ok(session);
    }

    @PostMapping("/cachedDecks/createCachedDecks")
    public ResponseEntity<CachedDecks> createCachedDecks(@RequestBody List<DeckCache> deckCaches,
            Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        CachedDecks decks = sessionService.createOrUpdateCachedDecks(currUserId, deckCaches);

        return ResponseEntity.ok(decks);
    }

    @PutMapping("/refreshSession")
    public ResponseEntity<Boolean> refreshSession(@RequestBody(required = false) Long additionalSeconds, Authentication authentication) {
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
    public ResponseEntity<CachedDecks> refreshCachedDecks(@RequestBody List<DeckCache> deckCaches, Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        CachedDecks refreshedCachedDecks = sessionService.refreshCachedDecks(currUserId, deckCaches);

        return ResponseEntity.ok(refreshedCachedDecks);
    }

    @PutMapping("/cachedDecks/update/{deckId}")
    public ResponseEntity<CachedDecks> updateCachedDeck(@PathVariable UUID deckId, @RequestBody DeckCache updatedDeckCache, Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        CachedDecks updatedCachedDecks = sessionService.updateSpecificDeckInCache(currUserId, deckId, updatedDeckCache);
        
        return ResponseEntity.ok(updatedCachedDecks);
    }

    @PutMapping("/cachedDecks/remove/{deckId}")
    public ResponseEntity<CachedDecks> removeDeckFromCache(@PathVariable UUID deckId, Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        CachedDecks updatedCachedDecks = sessionService.removeSingleDeckFromCache(currUserId, deckId);
        
        return ResponseEntity.ok(updatedCachedDecks);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> clearSessionAndCachedDecks(Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        sessionService.clearSessionAndCachedDecks(currUserId);

        return ResponseEntity.ok("Successfully cleared user's session and cached decks");
    }

    @DeleteMapping("/cachedDecks/invalidate")
    public ResponseEntity<String> invalidateCachedDecks(Authentication authentication) {
        UUID currUserId = UUID.fromString(authentication.getName());
        sessionService.invalidateCachedDecks(currUserId);
        
        return ResponseEntity.ok("Successfully deleted user's cached decks");
    }
}
