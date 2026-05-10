package com.example.backend.connection.admins;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record sendingInviteValidation(
        @NotBlank(message = "The email is mandatory") 
        @Email(message = "The email format is invalid") 
        String email) {
}
