package com.bob.server.auth.login;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    
    private String token;
    private String type = "Bearer";
    private String email;
    
    public LoginResponse() {
    }
    
    public LoginResponse(String token, String email) {
        this.token = token;
        this.email = email;
    }

}