package com.bob.server.repositories;

import java.time.Instant;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.server.model.Token;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByValue(String value);
    List<Token> findByUserId(UUID userId);
    List<Token> findByUserIdAndIsRevoked(UUID userId, boolean isRevoked);
    
    Optional<Token> findByValueAndIsRevokedFalseAndExpiresAtAfter(String value, Instant expiresAt);
    List<Token> findByUserIdAndIsRevokedFalseAndExpiresAtAfter(UUID userId, Instant expiresAt);
    boolean existsByUserIdAndIsRevokedFalseAndLastRenewedAtAfter(UUID userId, Instant lastRenewedAt);
}
