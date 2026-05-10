package com.example.backend.connection.admins;

import jakarta.validation.constraints.Size;

public record acceptInvitationValidation(
        @Size(min = 8, message = "The password must be at least 8 characters long") String password) {
}
