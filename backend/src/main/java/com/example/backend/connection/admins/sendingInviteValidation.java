package com.example.backend.connection.admins;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class sendingInviteValidation {
    private final sendingInviteRepo inviteRepo;

    public sendingInviteValidation(sendingInviteRepo inviteRepo) {
        this.inviteRepo = inviteRepo;
    }

    public void validate(
            @Email(message = "The email format is invalid") @NotBlank(message = "The email is mandatory") String email) {

        if (inviteRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
    }
}
