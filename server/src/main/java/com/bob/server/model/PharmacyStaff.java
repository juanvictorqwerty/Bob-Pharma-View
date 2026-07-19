package com.bob.server.model;

import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "pharmacy_staff",
            indexes = {
                @Index(name = "idx_pharmacy_staff_user_id", columnList = "user_id"),
                @Index(name = "idx_pharmacy_staff_pharmacy_id", columnList = "pharmacy_id")
            }
)

public class PharmacyStaff {
    
    @Id
    private UUID ID;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users userId;

    @ManyToOne
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacyId;

    @Column(name = "role", nullable = false)
    private String role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private String createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private String updatedAt;   

    @Column(name = "is_suspended", nullable = false)
    private boolean isSuspended=false;
}