package com.example.backend.connection.admins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admins")
public class sendingIinviteController {
    @Autowired
    private sendingInviteService inviteService;

    @PostMapping("/send-invite")
    public String sendInvite(@Valid @RequestBody sendingInviteValidation request) {
        return inviteService.sendInvite(request.email());
    }
}
