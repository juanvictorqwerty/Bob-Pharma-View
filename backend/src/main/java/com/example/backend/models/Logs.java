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
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Logs {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String user_id;

    @CreationTimestamp
    @Column(name = "action_time", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
