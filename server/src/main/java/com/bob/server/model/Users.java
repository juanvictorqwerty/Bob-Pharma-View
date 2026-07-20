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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users",
            indexes = {
                @Index(name = "idx_users_email", columnList = "email")
            }
        )

public class Users {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID ID;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked=false;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
