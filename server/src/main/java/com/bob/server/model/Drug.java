package com.bob.server.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Drug {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID ID;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "is_allowed", nullable = false)
    private boolean isAllowed=true;

    @Column(name = "created_at", nullable = false)
    private String createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

}
