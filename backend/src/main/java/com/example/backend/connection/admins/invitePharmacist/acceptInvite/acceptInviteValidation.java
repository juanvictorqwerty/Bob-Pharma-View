package com.example.backend.connection.admins.invitePharmacist.acceptInvite;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record acceptInviteValidation(
        @NotBlank(message = "Code is required") String code,
        @NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
        @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters long") String password) {

}
