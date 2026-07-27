package com.bob.server.auth.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bob.server.auth.token.JwtService;
import com.bob.server.auth.token.TokenService;
import com.bob.server.model.Code;
import com.bob.server.repositories.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(EmailController.class)
class EmailControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void createInviteWithValidDataShouldReturn201() throws Exception {
        EmailDTO dto = new EmailDTO();
        dto.setEmail("newuser@example.com");
        dto.setCategory("Admin");

        Code code = new Code();
        code.setEmail("newuser@example.com");
        code.setCategory("Admin");
        code.setCode("INV12345");
        code.setCreatedAt(Instant.now());
        code.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));

        when(emailService.createInvite(any(EmailDTO.class))).thenReturn(code);

        mockMvc.perform(post("/api/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createInviteWithMissingEmailShouldReturn400() throws Exception {
        EmailDTO dto = new EmailDTO();
        dto.setCategory("Admin");

        mockMvc.perform(post("/api/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInviteWithMissingCategoryShouldReturn400() throws Exception {
        EmailDTO dto = new EmailDTO();
        dto.setEmail("newuser@example.com");

        mockMvc.perform(post("/api/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInviteWhenServiceThrowsShouldReturn400() throws Exception {
        EmailDTO dto = new EmailDTO();
        dto.setEmail("newuser@example.com");
        dto.setCategory("Admin");

        when(emailService.createInvite(any(EmailDTO.class)))
                .thenThrow(new RuntimeException("Email already invited"));

        mockMvc.perform(post("/api/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already invited"));
    }

    @Test
    void resetPasswordWithValidEmailShouldReturn201() throws Exception {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO();
        dto.setEmail("user@example.com");

        Code resetCode = new Code();
        resetCode.setEmail("user@example.com");
        resetCode.setCategory("reset_password");
        resetCode.setCode("RESET123");
        resetCode.setCreatedAt(Instant.now());
        resetCode.setExpiresAt(Instant.now().plus(4, ChronoUnit.HOURS));

        when(emailService.createResetPasswordCode("user@example.com")).thenReturn(resetCode);

        mockMvc.perform(post("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void resetPasswordWithMissingEmailShouldReturn400() throws Exception {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO();

        mockMvc.perform(post("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPasswordWhenServiceThrowsShouldReturn400() throws Exception {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO();
        dto.setEmail("unknown@example.com");

        when(emailService.createResetPasswordCode("unknown@example.com"))
                .thenThrow(new RuntimeException("Email not found"));

        mockMvc.perform(post("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email not found"));
    }
}