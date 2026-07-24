package com.bob.server.repositories;

import java.time.Instant;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.server.model.Token;
import com.bob.server.model.Users;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByValue(String value);
    List<Token> findByUserId(Users userId);
    List<Token> findByUserIdAndIsRevoked(Users userId, boolean isRevoked);
    
    Optional<Token> findByValueAndIsRevokedFalseAndExpiresAtAfter(String value, Instant expiresAt);
    List<Token> findByUserIdAndIsRevokedFalseAndExpiresAtAfter(Users userId, Instant expiresAt);
    boolean existsByUserIdAndIsRevokedFalseAndLastRenewedAtAfter(Users userId, Instant lastRenewedAt);
}
