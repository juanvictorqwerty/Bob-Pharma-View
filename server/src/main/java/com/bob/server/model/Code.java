package com.bob.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "code",
        indexes = {
            @Index(name = "idx_code_email", columnList = "email"),
            @Index(name = "idx_code_code", columnList = "code", unique = true)
        }
)
@Getter
@Setter
public class Code {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        expiresAt = createdAt.plus(4, ChronoUnit.HOURS);
    }
}