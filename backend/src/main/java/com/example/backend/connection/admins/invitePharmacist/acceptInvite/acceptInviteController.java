package com.example.backend.connection.admins.invitePharmacist.acceptInvite;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/invite-pharmacist")

public class acceptInviteController {
    @PostMapping("/accept")
    public ResponseEntity<String> acceptInvite(@RequestBody acceptInviteValidation validation) {
        if (validation.code() == null || validation.code().isEmpty() || validation.email() == null
                || validation.email().isEmpty() || validation.password() == null || validation.password().isEmpty()) {
            return ResponseEntity.badRequest().body("All fields are required");
        }
        return ResponseEntity.ok().body("Invite accepted successfully");
    }
}
