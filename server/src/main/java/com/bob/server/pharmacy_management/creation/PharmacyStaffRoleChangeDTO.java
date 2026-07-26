package com.bob.server.pharmacy_management.creation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PharmacyStaffRoleChangeDTO {

    @NotBlank(message = "New role is required")
    private String newRole;
}