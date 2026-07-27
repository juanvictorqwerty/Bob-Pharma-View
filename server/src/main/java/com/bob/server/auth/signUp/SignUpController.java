package com.bob.server.auth.signUp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class SignUpController {
    
    private final SignUpService signUpService;
    
    public SignUpController(SignUpService signUpService) {
        this.signUpService = signUpService;
    }
    
    @PostMapping("/api/Signup-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminSignUpDTO adminSignUpDTO) {
        try {
            UserResponse response = signUpService.registerAdmin(adminSignUpDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/api/Signup-users")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpDTO signUpDTO) {
        try {
            UserResponse response = signUpService.registerUser(signUpDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
