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
@WebMvcTest(ChangePasswordController.class)
class ChangePasswordControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ChangePasswordService changePasswordService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void changePasswordWithValidDataShouldReturn200() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("oldPassword123");
        dto.setNewPassword("newPassword123");

        doNothing().when(changePasswordService).changePassword("oldPassword123", "newPassword123");

        mockMvc.perform(post("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));
    }

    @Test
    void changePasswordWithMissingCurrentPasswordShouldReturn400() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePasswordWithMissingNewPasswordShouldReturn400() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("oldPassword123");

        mockMvc.perform(post("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePasswordWhenServiceThrowsShouldReturn400() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("wrongPassword");
        dto.setNewPassword("newPassword123");

        doThrow(new RuntimeException("Current password is incorrect"))
                .when(changePasswordService).changePassword(anyString(), anyString());

        mockMvc.perform(post("/api/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Current password is incorrect"));
    }
}