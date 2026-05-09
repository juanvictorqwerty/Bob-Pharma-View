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

    public String confirmCode(String token) {

        String decodedToken = jwtUtils.extractSubject(token);

        Users user = repo.findById(decodedToken).orElse(null);
        if (user != null && user.isVerified()) {
            return "{\"responseCode\": 400, \"responseStatus\": \"Error\", \"message\": \"Email already verified\"}";
        }

        if (user != null && !user.isVerified()) {
            user.setVerified(true);
            repo.save(user);
            return "{\"responseCode\": 200, \"responseStatus\": \"Success\", \"message\": \"Email verified successfully\"}";
        }

        return "{\"responseCode\": 400, \"responseStatus\": \"Error\", \"message\": \"Invalid token\"}";
    }

}