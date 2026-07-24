package com.bob.server.auth.signUp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bob.server.auth.token.JwtService;
import com.bob.server.auth.token.TokenService;
import com.bob.server.model.Code;
import com.bob.server.model.Token;
import com.bob.server.model.Users;
import com.bob.server.repositories.CodeRepository;
import com.bob.server.repositories.UsersRepository;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private CodeRepository codeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private SignUpService signUpService;

    @Test
    void registerAdminRejectsInviteCodeWhenCategoryIsNotAdmin() {
        AdminSignUpDTO dto = new AdminSignUpDTO();
        dto.setEmail("admin@example.com");
        dto.setPassword("password123");
        dto.setInviteCode("INVITE123");

        when(usersRepository.existsByEmail("admin@example.com")).thenReturn(false);

        Code code = new Code();
        code.setEmail("admin@example.com");
        code.setCategory("User");
        code.setCode("INVITE123");
        code.setUsed(false);
        code.setCreatedAt(Instant.now());
        code.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        when(codeRepository.findByCodeIgnoreCase("INVITE123")).thenReturn(Optional.of(code));

        SignUpService.SignUpException exception = assertThrows(
                SignUpService.SignUpException.class,
                () -> signUpService.registerAdmin(dto)
        );

        assertEquals(SignUpValidation.INVITE_CODE_INVALID.getMessage(), exception.getMessage());
    }

    @Test
    void registerAdminAcceptsInviteCodeWhenEmailAndCategoryMatchIgnoringCase() {
        AdminSignUpDTO dto = new AdminSignUpDTO();
        dto.setEmail("NewUser@Example.com");
        dto.setPassword("password123");
        dto.setInviteCode("abc12345");

        when(usersRepository.existsByEmail("NewUser@Example.com")).thenReturn(false);

        Code code = new Code();
        code.setEmail("newuser@example.com");
        code.setCategory("ADMIN");
        code.setCode("ABC12345");
        code.setUsed(false);
        code.setCreatedAt(Instant.now());
        code.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        when(codeRepository.findByCodeIgnoreCase("ABC12345")).thenReturn(Optional.of(code));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(codeRepository.save(any(Code.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(tokenService.saveToken(anyString(), any(Users.class))).thenAnswer(invocation -> {
            Token token = new Token();
            token.setValue(invocation.getArgument(0));
            return token;
        });

        UserResponse response = signUpService.registerAdmin(dto);

        assertEquals("NewUser@Example.com", response.getEmail());
        assertEquals(true, code.isUsed());
    }
}
