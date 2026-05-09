package com.example.backend.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private String id;

    @CreationTimestamp
    @Column(columnDefinition = "DATETIME", name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "is_blocked", columnDefinition = "TINYINT(1)")
    private boolean isBlocked = false;

    @Column(name = "updated_at", columnDefinition = "DATETIME", nullable = true, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_verified", columnDefinition = "TINYINT(1)")
    private boolean isVerified = false;

}