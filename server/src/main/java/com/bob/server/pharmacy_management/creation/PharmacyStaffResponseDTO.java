package com.bob.server.pharmacy_management.creation;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PharmacyStaffResponseDTO {

    private UUID id;
    private String userEmail;
    private UUID pharmacyId;
    private String role;
    private String createdAt;
    private String updatedAt;
    private boolean isSuspended;
}