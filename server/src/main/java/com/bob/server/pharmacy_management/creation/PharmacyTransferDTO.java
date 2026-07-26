package com.bob.server.pharmacy_management.creation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PharmacyTransferDTO {

    @NotBlank(message = "New owner email is required")
    @Email(message = "Email should be valid")
    private String newOwnerEmail;
}