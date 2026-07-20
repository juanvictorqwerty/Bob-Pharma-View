package com.bob.server.auth.invite;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InviteController {
    
    private final InviteService inviteService;
    
    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }
    
    @PostMapping("/api/invite")
    public ResponseEntity<?> createInvite(@RequestBody InviteDTO inviteDTO) {
        try {
            return new ResponseEntity<>(inviteService.createInvite(inviteDTO), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
