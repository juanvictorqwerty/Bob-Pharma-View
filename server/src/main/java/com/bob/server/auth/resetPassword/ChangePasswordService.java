package com.bob.server.auth.resetPassword;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bob.server.model.Users;
import com.bob.server.repositories.UsersRepository;

@Service
public class ChangePasswordService {
    
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    
    public ChangePasswordService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public void changePassword(String currentPassword, String newPassword) {
        // Get the authenticated user from the JWT token
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You must be logged in to change password");
        }
        
        // The principal is the Users object
        Users user = (Users) authentication.getPrincipal();
        String email = user.getEmail();
        
        // Find the user in database
        user = usersRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // Verify current password matches
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Encode and update the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
    }
}