package com.bob.server.auth.signUp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    
    private String email;
    private String token;
    
    public UserResponse() {
    }
    
    public UserResponse(String email, String token) {
        this.email = email;
        this.token = token;
    }
    
}
