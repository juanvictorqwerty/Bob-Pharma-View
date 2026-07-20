package com.bob.server.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "token",
            indexes = {
                @Index(name = "idx_token_user_id", columnList = "user_id")
            }
)

public class Token {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID ID;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users userId;

    @Column(name = "value", nullable = false)
    private String value;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked=false;
}
