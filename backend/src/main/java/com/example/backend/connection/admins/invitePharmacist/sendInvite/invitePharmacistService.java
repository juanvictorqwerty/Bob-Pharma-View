package com.example.backend.connection.admins.invitePharmacist.sendInvite;

import com.example.backend.connection.superAdmins.SuperCreationRepo;
import com.example.backend.models.inviteCodes;

import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class invitePharmacistService {
    private final SuperCreationRepo superCreationRepo;
    private final invitePharmacistRepo invitePharmacistRepo;

    public invitePharmacistService(SuperCreationRepo superCreationRepo, invitePharmacistRepo invitePharmacistRepo) {
        this.superCreationRepo = superCreationRepo;
        this.invitePharmacistRepo = invitePharmacistRepo;
    }

    public ResponseEntity<String> invitePharmacist(invitePharmacistValidation validation) {

        if (superCreationRepo.existsByEmail(validation.email())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }

        String email = validation.email();

        Random random = new Random();
        String randomString = String.valueOf(random.nextInt(1000));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Invitation to join Bob Pharma View");
        message.setText("You are invited to join Bob Pharma View this is your secret code : " + randomString);

        inviteCodes inviteCodes = new inviteCodes();
        inviteCodes.setCode(randomString);
        inviteCodes.setEmail(email);
        inviteCodes.setRole("pharmacist");
        inviteCodes.setExpiryDate(java.time.LocalDateTime.now().plusDays(1));
        inviteCodes.setUsed(false);

        invitePharmacistRepo.save(inviteCodes);

        return ResponseEntity.status(HttpStatus.CREATED).body("Pharmacist invited successfully");
    }
}
