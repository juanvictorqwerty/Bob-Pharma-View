package com.example.backend.connection.superAdmins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.models.Users;

@Service
public class emailConfirmationService {

    @Autowired
    private SuperCreationRepo repo;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private JwtService jwtService; // Inject the service to generate the new token

    public String confirmCode(String token) {
        String decodedToken = jwtUtils.extractSubject(token);
        Users user = repo.findByEmail(decodedToken).orElse(null);

        if (user != null && user.isVerified()) {
            return "{\"responseCode\": 400, \"responseStatus\": \"Error\", \"message\": \"Email already verified\"}";
        }

        if (user != null && !user.isVerified()) {
            user.setVerified(true);
            repo.save(user);

            // Generate a 20-day token (20 * 24 * 60 * 60 * 1000 ms)
            long twentyDaysInMillis = 20L * 24 * 60 * 60 * 1000;
            String accessToken = jwtService.generateToken(decodedToken, twentyDaysInMillis);

            // Return success with the new access token
            return "{\"responseCode\": 200, \"responseStatus\": \"Success\", \"message\": \"Email verified successfully\", \"accessToken\": \""
                    + accessToken + "\"}";
        }

        return "{\"responseCode\": 400, \"responseStatus\": \"Error\", \"message\": \"Invalid token\"}";
    }
}