package com.bob.server.auth.login;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
@WebMvcTest(LoginController.class)
class LoginControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void loginWithValidCredentialsShouldReturn200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        LoginResponse response = new LoginResponse("jwt-token", "user@example.com");
        when(loginService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void loginWithMissingEmailShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setPassword("password123");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithMissingPasswordShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithInvalidEmailShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithInvalidCredentialsShouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong-password");

        when(loginService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}