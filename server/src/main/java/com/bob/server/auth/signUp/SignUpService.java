package com.bob.server.auth.signUp;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bob.server.auth.token.JwtService;
import com.bob.server.model.Code;
import com.bob.server.model.Users;
import com.bob.server.repositories.CodeRepository;
import com.bob.server.repositories.UsersRepository;

@Service
public class SignUpService {
    
    private final UsersRepository usersRepository;
    private final CodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public SignUpService(UsersRepository usersRepository, CodeRepository codeRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usersRepository = usersRepository;
        this.codeRepository = codeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    public UserResponse registerUser(SignUpDTO signUpDTO) {
        String email = signUpDTO.getEmail();
        
        if (usersRepository.existsByEmail(email)) {
            throw new RuntimeException(SignUpValidation.EMAIL_ALREADY_EXISTS.getMessage());
        }
        
        Users user = new Users();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(signUpDTO.getPassword()));
        user.setRole("user");
        user.setBlocked(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        
        Users savedUser = usersRepository.save(user);
        
        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole());
        
        return new UserResponse(savedUser.getEmail(), token);
    }
    
    public UserResponse registerAdmin(AdminSignUpDTO adminSignUpDTO) {
        String email = adminSignUpDTO.getEmail();
        String inviteCode = adminSignUpDTO.getInviteCode();
        
        if (usersRepository.existsByEmail(email)) {
            throw new RuntimeException(SignUpValidation.EMAIL_ALREADY_EXISTS.getMessage());
        }
        
        Code code = codeRepository.findByCode(inviteCode)
            .orElseThrow(() -> new RuntimeException(SignUpValidation.INVITE_CODE_INVALID.getMessage()));
        
        if (code.isUsed()) {
            throw new RuntimeException(SignUpValidation.INVITE_CODE_ALREADY_USED.getMessage());
        }
        
        if (code.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException(SignUpValidation.INVITE_CODE_EXPIRED.getMessage());
        }
        
        if (!code.getEmail().equals(email)) {
            throw new RuntimeException(SignUpValidation.INVITE_CODE_INVALID.getMessage());
        }
        
        Users user = new Users();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(adminSignUpDTO.getPassword()));
        user.setRole("Admin");
        user.setBlocked(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        
        Users savedUser = usersRepository.save(user);
        
        code.setUsed(true);
        codeRepository.save(code);
        
        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole());
        
        return new UserResponse(savedUser.getEmail(), token);
    }
}