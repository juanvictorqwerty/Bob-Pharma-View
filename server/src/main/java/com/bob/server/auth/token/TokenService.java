package com.bob.server.auth.token;

import com.bob.server.model.Token;
import com.bob.server.model.Users;
import com.bob.server.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.expiration:2592000000}")
    private long jwtExpiration;

    public TokenService(TokenRepository tokenRepository, JwtService jwtService) {
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
    }

    public Token saveToken(String tokenValue, Users user) {
        // Revoke all existing active tokens for this user
        revokeAllUserTokens(user);

        // Create new token
        Token token = new Token();
        token.setValue(tokenValue);
        token.setUserId(user);
        token.setExpiresAt(Instant.now().plus(jwtExpiration, ChronoUnit.MILLIS));
        token.setRevoked(false);
        token.setLastRenewedAt(Instant.now());

        return tokenRepository.save(token);
    }

    public void revokeAllUserTokens(Users user) {
        List<Token> validUserTokens = tokenRepository.findByUserIdAndIsRevokedFalseAndExpiresAtAfter(
                user, Instant.now());
        
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> token.setRevoked(true));
            tokenRepository.saveAll(validUserTokens);
        }
    }

    public Token validateToken(String tokenValue) {
        Token token = tokenRepository.findByValueAndIsRevokedFalseAndExpiresAtAfter(
                tokenValue, Instant.now())
                .orElse(null);
        
        return token;
    }

    public Token renewToken(String tokenValue) {
        Token oldToken = tokenRepository.findByValueAndIsRevokedFalseAndExpiresAtAfter(
                tokenValue, Instant.now())
                .orElse(null);

        if (oldToken == null) {
            return null;
        }

        // Check if token was renewed today
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        if (oldToken.getLastRenewedAt() != null && 
            oldToken.getLastRenewedAt().isAfter(todayStart)) {
            // Already renewed today, return the same token
            return oldToken;
        }

        // Revoke old token
        oldToken.setRevoked(true);
        tokenRepository.save(oldToken);

        // Get user from old token
        Users user = oldToken.getUserId();

        // Generate new JWT token with 1 month + 1 day expiration
        long newExpiration = jwtExpiration + (24 * 60 * 60 * 1000); // Add 1 day
        String newTokenValue = jwtService.generateTokenWithExpiration(
                user.getEmail(), 
                user.getRole(), 
                newExpiration
        );

        // Create new token record
        Token newToken = new Token();
        newToken.setValue(newTokenValue);
        newToken.setUserId(user);
        newToken.setExpiresAt(Instant.now().plus(newExpiration, ChronoUnit.MILLIS));
        newToken.setRevoked(false);
        newToken.setLastRenewedAt(Instant.now());

        return tokenRepository.save(newToken);
    }

    public boolean canRenewToday(Users user) {
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return !tokenRepository.existsByUserIdAndIsRevokedFalseAndLastRenewedAtAfter(
                user, todayStart);
    }
}