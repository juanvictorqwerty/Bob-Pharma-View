package com.example.backend.connection.admins.invitePharmacist.acceptInvite;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/invite-pharmacist")

public class acceptInviteController {
    private final acceptInviteService acceptInviteService;

    public acceptInviteController(acceptInviteService acceptInviteService) {
        this.acceptInviteService = acceptInviteService;
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptInvite(@RequestBody acceptInviteValidation validation) {
        return acceptInviteService.acceptInvite(validation);
    }
}
