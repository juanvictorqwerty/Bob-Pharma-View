package com.example.backend.connection.admins;

public class sendingInviteValidation {
    private final sendingInviteRepo inviteRepo;

    public sendingInviteValidation(sendingInviteRepo inviteRepo) {
        this.inviteRepo = inviteRepo;
    }

    public void validate(String email) {
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new RuntimeException("Email is invalid");
        }

        if (inviteRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
    }
}
