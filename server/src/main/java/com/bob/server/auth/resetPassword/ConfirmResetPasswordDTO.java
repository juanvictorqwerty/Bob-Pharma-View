package com.bob.server.auth.resetPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ConfirmResetPasswordDTO {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Code is required")
    private String code;
    
    @NotBlank(message = "New password is required")
    private String newPassword;
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}