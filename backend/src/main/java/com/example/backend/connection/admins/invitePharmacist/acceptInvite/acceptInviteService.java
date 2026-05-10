package com.example.backend.connection.admins.invitePharmacist.acceptInvite;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.connection.admins.invitePharmacist.sendInvite.invitePharmacistRepo;
import com.example.backend.connection.superAdmins.JwtService;
import com.example.backend.models.Users;
import com.example.backend.models.inviteCodes;

@Service
public class acceptInviteService {
    private final acceptInviteRepo acceptInviteRepo;
    private final invitePharmacistRepo invitePharmacistRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public acceptInviteService(acceptInviteRepo acceptInviteRepo, invitePharmacistRepo invitePharmacistRepo,
            BCryptPasswordEncoder passwordEncoder, JwtService jwtService) {
        this.acceptInviteRepo = acceptInviteRepo;
        this.invitePharmacistRepo = invitePharmacistRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ResponseEntity<String> acceptInvite(acceptInviteValidation validation) {
        if (validation.code() == null || validation.code().isEmpty() || validation.email() == null
                || validation.email().isEmpty() || validation.password() == null
                || validation.password().isEmpty()) {
            return ResponseEntity.badRequest().body("All fields are required");
        }

        // 1. Look up the invite code
        Optional<inviteCodes> inviteCodeOpt = invitePharmacistRepo.findByCode(validation.code());
        if (inviteCodeOpt.isEmpty()) {
            // Code is incorrect — find by email and burn it
            Optional<inviteCodes> byEmail = invitePharmacistRepo.findByEmail(validation.email());
            if (byEmail.isPresent()) {
                inviteCodes codeToInvalidate = byEmail.get();
                codeToInvalidate.setUsed(true);
                invitePharmacistRepo.save(codeToInvalidate);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid invite code, request another token");
        }

        inviteCodes inviteCode = inviteCodeOpt.get();

        // 2. Check if the code has already been used
        if (inviteCode.isUsed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invite code has already been used");
        }

        // 3. Check if the code has expired
        if (inviteCode.getExpiryDate() != null && inviteCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invite code has expired");
        }

        // 4. Check that the email matches the invitation
        if (!inviteCode.getEmail().equals(validation.email())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email does not match the invitation");
        }

        // 5. Check if a user with this email already exists
        Optional<Users> existingUser = acceptInviteRepo.findByEmail(validation.email());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with this email already exists");
        }

        // 6. Create and save the new user
        Users user = new Users();
        user.setEmail(validation.email());
        user.setPassword(passwordEncoder.encode(validation.password()));
        user.setRole(inviteCode.getRole());
        user.setVerified(true);
        acceptInviteRepo.save(user);

        // 7. Mark the invite code as used
        inviteCode.setUsed(true);
        invitePharmacistRepo.save(inviteCode);

        // 8. Generate and return a JWT token (24 hours expiry)
        // Génération d'un token valide pour 30 jours (2592000000L)
        String token = jwtService.generateToken(user.getEmail(), 2592000000L);

        return ResponseEntity.ok().body(token);
    }
}
