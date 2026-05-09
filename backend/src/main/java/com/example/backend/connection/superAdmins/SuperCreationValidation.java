package com.example.backend.connection.superAdmins;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuperCreationValidation(
        @Email(message = "The email format is invalid") @NotBlank(message = "The email is mandatory") String email,

        @NotBlank(message = "The secret code is mandatory") String secretCode,

        @Size(min = 8, message = "The password must be at least 8 characters long") String password) {
}
