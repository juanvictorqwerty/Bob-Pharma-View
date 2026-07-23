package com.bob.server.auth.login;

import com.bob.server.auth.token.JwtService;
import com.bob.server.auth.token.TokenService;
import com.bob.server.config.AuthenticationException;
import com.bob.server.model.Token;
import com.bob.server.model.Users;
import com.bob.server.repositories.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;

    public LoginService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, JwtService jwtService, TokenService tokenService) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        Users user = usersRepository.findByEmail(request.getEmail());
        
        if (user == null) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Manually validate password (avoid AuthenticationManager to prevent infinite recursion)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        // Save token to database
        Token savedToken = tokenService.saveToken(token, user);

        return new LoginResponse(savedToken.getValue(), user.getEmail());
    }
}
