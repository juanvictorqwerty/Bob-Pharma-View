package com.example.backend.connection.admins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.backend.connection.superAdmins.ApiResponse;

@RestController
@RequestMapping("/admins")
public class acceptInvitationController {

    @Autowired
    private acceptInvitationService inviteService;

    @PostMapping("/accept-invitation")
    public ResponseEntity<ApiResponse> acceptInvitation(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody acceptInvitationValidation validation) {
        
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "Error", "Authorization token missing or invalid"));
        }
        
        return inviteService.acceptInvitation(token, validation);
    }
}
