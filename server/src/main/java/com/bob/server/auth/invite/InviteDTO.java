package com.bob.server.auth.invite;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class InviteDTO {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
}