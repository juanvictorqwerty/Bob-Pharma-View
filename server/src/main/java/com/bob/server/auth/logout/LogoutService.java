package com.bob.server.auth.logout;

import com.bob.server.model.Token;
import com.bob.server.model.Users;
import com.bob.server.repositories.TokenRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {
    
    private final TokenRepository tokenRepository;
    
    public LogoutService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }
    
    public void logoutCurrentToken(String tokenValue) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You must be logged in to logout");
        }
        
        // Revoke the specific token
        Token token = tokenRepository.findByValueAndIsRevokedFalseAndExpiresAtAfter(
                tokenValue, java.time.Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Token not found or already revoked"));
        
        token.setRevoked(true);
        tokenRepository.save(token);
    }
    
    public void logoutAllTokens() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You must be logged in to logout");
        }
        
        Users user = (Users) authentication.getPrincipal();
        revokeAllUserTokens(user);
    }
    
    private void revokeAllUserTokens(Users user) {
        var validUserTokens = tokenRepository.findByUserIdAndIsRevokedFalseAndExpiresAtAfter(
                user, java.time.Instant.now());
        
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> token.setRevoked(true));
            tokenRepository.saveAll(validUserTokens);
        }
    }
}