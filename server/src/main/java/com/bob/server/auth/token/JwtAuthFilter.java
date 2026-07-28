package com.bob.server.auth.token;

import com.bob.server.model.Users;
import com.bob.server.repositories.UsersRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final TokenService tokenService;

    public JwtAuthFilter(JwtService jwtService, UsersRepository usersRepository, TokenService tokenService) {
        this.jwtService = jwtService;
        this.usersRepository = usersRepository;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;

        // Skip JWT validation for public endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/api/login") || 
            path.startsWith("/api/Signup-admin") || 
            path.startsWith("/api/Signup-users") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            email = jwtService.extractUsername(jwt);
            
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Users user = usersRepository.findByEmail(email);
                
                if (user != null && jwtService.isTokenValid(jwt)) {
                    // Validate token against database
                    var tokenFromDb = tokenService.validateToken(jwt);
                    
                    if (tokenFromDb != null) {
                        // Token is valid in database, set authentication
                        String role = user.getRole() == null ? "user" : user.getRole();
                        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user.getEmail(), null, authorities);
                        
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        // Check if token needs renewal (once per day)
                        var renewedToken = tokenService.renewToken(jwt);
                        if (renewedToken != null && !renewedToken.getValue().equals(jwt)) {
                            // Token was renewed, add new token to response header
                            response.setHeader("Renewed-Token", renewedToken.getValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("JWT authentication error: " + e.getMessage());
            // Don't set authentication on error, just continue the filter chain
        }

        filterChain.doFilter(request, response);
    }
}
