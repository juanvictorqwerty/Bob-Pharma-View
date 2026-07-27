package com.bob.server.auth.resetPassword;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bob.server.auth.token.JwtService;
import com.bob.server.auth.token.TokenService;
import com.bob.server.repositories.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ResetPasswordController.class)
class ResetPasswordControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ResetPasswordService resetPasswordService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void confirmResetPasswordWithValidDataShouldReturn200() throws Exception {
        ConfirmResetPasswordDTO dto = new ConfirmResetPasswordDTO();
        dto.setEmail("user@example.com");
        dto.setCode("RESET123");
        dto.setNewPassword("newPassword123");

        doNothing().when(resetPasswordService).confirmResetPassword("user@example.com", "RESET123", "newPassword123");

        mockMvc.perform(post("/api/reset-password/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully"));
    }

    @Test
    void confirmResetPasswordWithMissingEmailShouldReturn400() throws Exception {
        ConfirmResetPasswordDTO dto = new ConfirmResetPasswordDTO();
        dto.setCode("RESET123");
        dto.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/reset-password/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmResetPasswordWithMissingCodeShouldReturn400() throws Exception {
        ConfirmResetPasswordDTO dto = new ConfirmResetPasswordDTO();
        dto.setEmail("user@example.com");
        dto.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/reset-password/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmResetPasswordWithMissingNewPasswordShouldReturn400() throws Exception {
        ConfirmResetPasswordDTO dto = new ConfirmResetPasswordDTO();
        dto.setEmail("user@example.com");
        dto.setCode("RESET123");

        mockMvc.perform(post("/api/reset-password/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmResetPasswordWhenServiceThrowsShouldReturn400() throws Exception {
        ConfirmResetPasswordDTO dto = new ConfirmResetPasswordDTO();
        dto.setEmail("user@example.com");
        dto.setCode("INVALID");
        dto.setNewPassword("newPassword123");

        doThrow(new RuntimeException("Invalid or expired code"))
                .when(resetPasswordService).confirmResetPassword(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/reset-password/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or expired code"));
    }
}