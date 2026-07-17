package com.bob.server.repositories;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bob.server.model.Token;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByValue(String value);
    List<Token> findByUserId(UUID userId);
    List<Token> findByUserIdAndIsRevoked(UUID userId, boolean isRevoked);
}