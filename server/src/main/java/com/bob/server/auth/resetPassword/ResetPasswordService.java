package com.bob.server.auth.resetPassword;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bob.server.model.Code;
import com.bob.server.model.Users;
import com.bob.server.repositories.CodeRepository;
import com.bob.server.repositories.UsersRepository;

@Service
public class ResetPasswordService {
    
    private final CodeRepository codeRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    
    public ResetPasswordService(CodeRepository codeRepository, UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.codeRepository = codeRepository;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public void confirmResetPassword(String email, String code, String newPassword) {
        // Verify the reset code exists, is unused, and not expired
        Code codeEntity = codeRepository.findByEmailAndCodeAndCategory(email, code, "reset_password")
            .orElseThrow(() -> new IllegalArgumentException("Invalid reset code"));
        
        if (codeEntity.isUsed()) {
            throw new IllegalStateException("Reset code has already been used");
        }
        
        if (codeEntity.getExpiresAt().isBefore(java.time.Instant.now())) {
            throw new IllegalStateException("Reset code has expired");
        }
        
        // Find the user
        Users user = usersRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // Encode and update the password
        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
        
        // Mark the code as used
        codeEntity.setUsed(true);
        codeRepository.save(codeEntity);
    }
}