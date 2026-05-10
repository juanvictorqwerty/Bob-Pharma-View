package com.example.backend.connection.superAdmins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("api/superadmin/creation")
public class SuperCreationController {

    @Autowired
    private SuperCreationService superCreationService;

    @Autowired
    private sendConfirmationTokenService emailVerificationService;

    @PostMapping("create_superAdmin")
    public ResponseEntity<ApiResponse> createSuperAdmin(
            @Validated @RequestBody SuperCreationValidation superCreationValidation) {

        return superCreationService.createSuperAdmin(superCreationValidation);
    }

    @GetMapping("check_email")
    public String checkEmail(@RequestParam String email) {
        return emailVerificationService.sendConfirmationToken(email);
    }
}
