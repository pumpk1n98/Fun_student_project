package com.example.student.controller;

import com.example.student.dto.AuthResponse;
import com.example.student.dto.LoginRequest;
import com.example.student.model.Role;
import com.example.student.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthController controller = new AuthController(authenticationManager, jwtService);

    @Test
    void authenticatesAndReturnsBearerTokenAndRoles() {
        LoginRequest request = new LoginRequest("admin@example.com", "password");
        User user = new User("admin@example.com", "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(jwtService.generateToken("admin@example.com", Set.of(Role.ADMIN))).thenReturn("signed-token");

        AuthResponse response = controller.login(request);

        assertEquals("signed-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(3600, response.getExpiresIn());
        assertEquals(List.of("ROLE_ADMIN"), response.getRoles());
        verify(jwtService).generateToken("admin@example.com", Set.of(Role.ADMIN));
    }
}
