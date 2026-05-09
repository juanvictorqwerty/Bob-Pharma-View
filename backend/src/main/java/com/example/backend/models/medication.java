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
public class medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "medication_id")
    private String id;

    @Column(name = "medication_name", nullable = false)
    private String name;

    @Column(name = "medication_description", nullable = false)
    private String description;

    @Column(name = "medication_allowed", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isAllowed = true;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME", nullable = true, updatable = false)
    private LocalDateTime updatedAt;

}
