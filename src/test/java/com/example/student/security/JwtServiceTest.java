package com.example.student.security;

import com.example.student.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService();

    @Test
    void generatesAndValidatesTokenWithRoles() {
        String token = jwtService.generateToken("admin@example.com", Set.of(Role.ADMIN));
        User user = new User("admin@example.com", "password", Set.of());

        assertEquals("admin@example.com", jwtService.extractUsername(token));
        assertEquals(Set.of(Role.ADMIN), jwtService.extractRoles(token));
        assertTrue(jwtService.validateToken(token, user));
    }

    @Test
    void rejectsTokenForDifferentUser() {
        String token = jwtService.generateToken("admin@example.com", Set.of(Role.ADMIN));
        User otherUser = new User("teacher@example.com", "password", Set.of());

        assertFalse(jwtService.validateToken(token, otherUser));
    }
}
