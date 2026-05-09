package com.example.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pharmacy_id")
    private String id;

    @Column(name = "pharmacy_name", nullable = false)
    private String name;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "pharmacy_location", columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pharmacy_admin_list", nullable = false)
    private List<String> adminList;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pharmacy_staff_list", nullable = false)
    private List<String> staffList;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "pharma_contact_number", nullable = false)
    private Long contactNumber;

    // Utilisation de Map pour représenter un objet JSON complexe
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "guard_program")
    private Map<String, Object> guardProgram;

}
