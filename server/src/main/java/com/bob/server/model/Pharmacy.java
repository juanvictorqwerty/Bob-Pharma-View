package com.bob.server.model;

import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "pharmacy",
            indexes = {
                @Index(name = "idx_pharmacy_name", columnList = "name"),
                @Index(name = "idx_pharmacy_latitude", columnList = "latitude"),
                @Index(name = "idx_pharmacy_longitude", columnList = "longitude")
            }
        )

public class Pharmacy {
    
    @Id
    private UUID ID;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private Users creatorId;

    @Column(name = "region", nullable = false)
    private String region;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "longitude", nullable = false)
    private String longitude;
    
    @Column(name = "location", nullable = true)
    private Point location;
    
    @Column(name = "latitude", nullable = false)
    private String latitude;

    @Column(name = "is_approved", nullable = false)
    private boolean isApproved=false;
    
    @Column(name = "is_suspended", nullable = false)
    private boolean isSuspended=false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive=true;

}
