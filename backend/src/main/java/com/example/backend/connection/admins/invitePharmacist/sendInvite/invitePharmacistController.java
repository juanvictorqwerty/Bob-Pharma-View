package com.example.backend.connection.admins.invitePharmacist.sendInvite;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admins/invite-pharmacist")
public class invitePharmacistController {
    private final invitePharmacistService invitePharmacistService;

    public invitePharmacistController(invitePharmacistService invitePharmacistService) {
        this.invitePharmacistService = invitePharmacistService;
    }

    @PostMapping
    public ResponseEntity<String> invitePharmacist(@Valid @RequestBody invitePharmacistValidation validation) {
        return invitePharmacistService.invitePharmacist(validation);
    }
}
