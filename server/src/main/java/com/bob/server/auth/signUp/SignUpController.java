package com.bob.server.auth.signUp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignUpController {
    
    private final SignUpService signUpService;
    
    public SignUpController(SignUpService signUpService) {
        this.signUpService = signUpService;
    }
    
    @PostMapping("/api/Signup-admin")
    public ResponseEntity<UserResponse> registerAdmin(@RequestBody AdminSignUpDTO adminSignUpDTO) {
        UserResponse response = signUpService.registerAdmin(adminSignUpDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/api/Signup-users")
    public ResponseEntity<UserResponse> registerUser(@RequestBody SignUpDTO signUpDTO) {
        UserResponse response = signUpService.registerUser(signUpDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
