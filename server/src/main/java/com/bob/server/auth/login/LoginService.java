package com.bob.server.auth.login;

import com.bob.server.auth.token.JwtService;
import com.bob.server.model.Users;
import com.bob.server.repositories.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Users user = usersRepository.findByEmail(request.getEmail());
        
        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }

        // Manually validate password (avoid AuthenticationManager to prevent infinite recursion)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return new LoginResponse(token, user.getEmail());
    }
}
