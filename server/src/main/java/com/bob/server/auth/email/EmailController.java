package com.bob.server.auth.email;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {
    
    private final EmailService inviteService;
    
    public EmailController(EmailService inviteService) {
        this.inviteService = inviteService;
    }
    
    @PostMapping("/api/invite")
    public ResponseEntity<?> createInvite(@RequestBody EmailDTO inviteDTO) {
        try {
            return new ResponseEntity<>(inviteService.createInvite(inviteDTO), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/api/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO resetPasswordRequest) {
        try {
            return new ResponseEntity<>(inviteService.createResetPasswordCode(resetPasswordRequest.getEmail()), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
