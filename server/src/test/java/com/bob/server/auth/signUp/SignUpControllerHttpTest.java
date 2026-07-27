package com.bob.server.auth.signUp;

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
@WebMvcTest(SignUpController.class)
class SignUpControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SignUpService signUpService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void registerAdminWithValidDataShouldReturn201() throws Exception {
        AdminSignUpDTO dto = new AdminSignUpDTO();
        dto.setEmail("admin@example.com");
        dto.setPassword("password123");
        dto.setInviteCode("INVITE123");

        UserResponse response = new UserResponse("admin@example.com", "jwt-token");
        when(signUpService.registerAdmin(any(AdminSignUpDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/Signup-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerAdminWithMissingEmailShouldReturn400() throws Exception {
        AdminSignUpDTO dto = new AdminSignUpDTO();
        dto.setPassword("password123");
        dto.setInviteCode("INVITE123");

        mockMvc.perform(post("/api/Signup-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerAdminWithMissingInviteCodeShouldReturn400() throws Exception {
        AdminSignUpDTO dto = new AdminSignUpDTO();
        dto.setEmail("admin@example.com");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/Signup-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerAdminWithShortPasswordShouldReturn400() throws Exception {
        AdminSignUpDTO dto = new AdminSignUpDTO();
        dto.setEmail("admin@example.com");
        dto.setPassword("12345");
        dto.setInviteCode("INVITE123");

        mockMvc.perform(post("/api/Signup-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUserWithValidDataShouldReturn201() throws Exception {
        SignUpDTO dto = new SignUpDTO();
        dto.setEmail("user@example.com");
        dto.setPassword("password123");

        UserResponse response = new UserResponse("user@example.com", "jwt-token");
        when(signUpService.registerUser(any(SignUpDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/Signup-users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerUserWithMissingPasswordShouldReturn400() throws Exception {
        SignUpDTO dto = new SignUpDTO();
        dto.setEmail("user@example.com");

        mockMvc.perform(post("/api/Signup-users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUserWithInvalidEmailShouldReturn400() throws Exception {
        SignUpDTO dto = new SignUpDTO();
        dto.setEmail("invalid-email");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/Signup-users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}