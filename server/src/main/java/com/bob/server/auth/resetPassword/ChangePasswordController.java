package com.bob.server.auth.resetPassword;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class ChangePasswordController {
    
    private final ChangePasswordService changePasswordService;
    
    public ChangePasswordController(ChangePasswordService changePasswordService) {
        this.changePasswordService = changePasswordService;
    }
    
    @PostMapping("/api/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            changePasswordService.changePassword(
                changePasswordDTO.getCurrentPassword(),
                changePasswordDTO.getNewPassword()
            );
            return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}