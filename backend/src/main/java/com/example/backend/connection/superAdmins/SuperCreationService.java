package com.example.backend.connection.superAdmins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.models.Users;

import jakarta.transaction.Transactional;

@Service
public class SuperCreationService {

    @Value("${ADMIN_SECRET_KEY}")
    private String systemSecret;

    @Autowired
    private SuperCreationRepo superCreationRepo;

    @Autowired
    private emailConfirmationService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<ApiResponse> createSuperAdmin(SuperCreationValidation validation) {
        if (!validation.secretCode().equals(systemSecret)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(400, "Error", "Invalid secret code"));
        }

        if (superCreationRepo.existsById(validation.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(409, "Error", "Email already exists"));
        }

        Users user = new Users();
        user.setEmail(validation.email());
        user.setPassword(passwordEncoder.encode(validation.password()));
        user.setRole("super_admin");
        user.setVerified(false);

        superCreationRepo.save(user);
        emailService.sendConfirmationToken(validation.email());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(201, "Success", "SuperAdmin created successfully"));
    }
}