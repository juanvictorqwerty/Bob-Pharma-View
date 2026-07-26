package com.bob.server.auth.logout;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bob.server.auth.token.JwtService;
import com.bob.server.auth.token.TokenService;
import com.bob.server.repositories.UsersRepository;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(LogoutController.class)
class LogoutControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogoutService logoutService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void logoutWithValidTokenShouldReturn200() throws Exception {
        doNothing().when(logoutService).logoutCurrentToken("valid-token");

        mockMvc.perform(post("/api/logout")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));
    }

    @Test
    void logoutWithoutTokenShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No token provided"));
    }

    @Test
    void logoutWithInvalidAuthHeaderShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/logout")
                .header("Authorization", "InvalidHeader"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No token provided"));
    }

    @Test
    void logoutWhenServiceThrowsShouldReturn400() throws Exception {
        doThrow(new RuntimeException("Token expired"))
                .when(logoutService).logoutCurrentToken("expired-token");

        mockMvc.perform(post("/api/logout")
                .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Token expired"));
    }

    @Test
    void logoutAllShouldReturn200() throws Exception {
        doNothing().when(logoutService).logoutAllTokens();

        mockMvc.perform(post("/api/logout/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out from all devices successfully"));
    }

    @Test
    void logoutAllWhenServiceThrowsShouldReturn400() throws Exception {
        doThrow(new RuntimeException("Something went wrong"))
                .when(logoutService).logoutAllTokens();

        mockMvc.perform(post("/api/logout/all"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Something went wrong"));
    }
}