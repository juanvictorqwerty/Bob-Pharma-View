package com.example.backend.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(name = "medication_allowed", nullable = false, columnDefinition = "BOOLEAN")
    private boolean isAllowed = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

}
