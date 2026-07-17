package com.bob.server.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "stock",
            indexes = {
                @Index(name = "idx_stock_pharmacy_id", columnList = "pharmacy_id"),
                @Index(name = "idx_stock_drug_id", columnList = "drug_id")
            }
)

public class Stock {
    
    @Id
    private UUID ID;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "pharmacy_id", nullable = false)
    private UUID pharmacyId;

    @Column(name = "drug_id", nullable = false)
    private UUID drugId;

    @Column(name = "quantity", nullable = false)
    private int quantity;
}
