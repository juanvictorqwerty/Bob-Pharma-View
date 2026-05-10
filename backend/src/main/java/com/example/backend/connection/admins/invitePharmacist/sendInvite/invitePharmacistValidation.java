package com.example.backend.connection.admins.invitePharmacist.sendInvite;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record invitePharmacistValidation(
                @NotBlank(message = "Email is required") @Email(message = "Email is required") String email) {

}
