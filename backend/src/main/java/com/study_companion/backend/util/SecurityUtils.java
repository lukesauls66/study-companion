package com.study_companion.backend.util;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
    
    public boolean canAccess(Authentication authentication, UUID userId) {
        if (isAdmin(authentication)) {
            return true;
        }
        
        UUID requestingUserId = UUID.fromString(authentication.getName());
        return requestingUserId.equals(userId);
    }
}