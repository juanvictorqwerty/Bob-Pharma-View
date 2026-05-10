package com.example.backend.connection.admins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.connection.superAdmins.ApiResponse;
import com.example.backend.connection.superAdmins.JwtService;
import com.example.backend.models.Users;

import jakarta.transaction.Transactional;

@Service
public class acceptInvitationService {

    @Autowired
    private acceptInvitationRepo inviteRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Transactional
    public ResponseEntity<ApiResponse> acceptInvitation(String token, acceptInvitationValidation validation) {
        try {
            String email = jwtService.extractEmail(token);

            if (inviteRepo.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse(409, "Error", "Email already exists"));
            }

            Users user = new Users();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(validation.password()));
            user.setRole("admin");
            user.setVerified(true);

            inviteRepo.save(user);

            String newToken = jwtService.generateToken(email, 3600 * 24 * 30);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(200, "Success", newToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(400, "Error", "Invalid or expired token"));
        }
    }
}