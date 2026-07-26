package com.bob.server.pharmacy_management.creation;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PharmacyResponseDTO {

    private UUID id;
    private String name;
    private String region;
    private String city;
    private String latitude;
    private String longitude;
    private String createdAt;
    private String updatedAt;
    private boolean isApproved;
    private boolean isSuspended;
    private boolean isActive;
}