package com.bob.server.auth.resetPassword;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResetPasswordController {
    
    private final ResetPasswordService resetPasswordService;
    
    public ResetPasswordController(ResetPasswordService resetPasswordService) {
        this.resetPasswordService = resetPasswordService;
    }
    
    @PostMapping("/api/reset-password/confirm")
    public ResponseEntity<?> confirmResetPassword(@RequestBody ConfirmResetPasswordDTO confirmResetPasswordDTO) {
        try {
            resetPasswordService.confirmResetPassword(
                confirmResetPasswordDTO.getEmail(),
                confirmResetPasswordDTO.getCode(),
                confirmResetPasswordDTO.getNewPassword()
            );
            return new ResponseEntity<>("Password reset successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}