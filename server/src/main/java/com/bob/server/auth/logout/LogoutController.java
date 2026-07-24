package com.bob.server.auth.logout;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LogoutController {
    
    private final LogoutService logoutService;
    
    public LogoutController(LogoutService logoutService) {
        this.logoutService = logoutService;
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ResponseEntity<>("No token provided", HttpStatus.BAD_REQUEST);
            }
            
            String token = authHeader.substring(7);
            logoutService.logoutCurrentToken(token);
            return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/logout/all")
    public ResponseEntity<?> logoutAll() {
        try {
            logoutService.logoutAllTokens();
            return new ResponseEntity<>("Logged out from all devices successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}