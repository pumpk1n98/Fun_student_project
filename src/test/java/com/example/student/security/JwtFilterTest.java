package com.example.student.security;

import com.example.student.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtFilterTest {
    private final CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final JwtFilter filter = new JwtFilter(userDetailsService, jwtService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void continuesWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void authenticatesValidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        User user = new User("admin@example.com", "encoded", Set.of());
        when(jwtService.extractUsername("token")).thenReturn("admin@example.com");
        when(userDetailsService.loadUserByUsername("admin@example.com")).thenReturn(user);
        when(jwtService.validateToken("token", user)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void returnsUnauthorizedForInvalidApiToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/student");
        request.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(jwtService.extractUsername("invalid")).thenThrow(new IllegalArgumentException("bad token"));

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void propagatesInvalidTokenErrorOutsideApi() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/");
        request.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(jwtService.extractUsername("invalid")).thenThrow(new IllegalArgumentException("bad token"));

        assertThrows(IllegalArgumentException.class, () -> filter.doFilter(request, response, chain));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void doesNotReplaceAnExistingAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("existing", null));
        when(jwtService.extractUsername("token")).thenReturn("admin@example.com");

        filter.doFilter(request, response, chain);

        verify(userDetailsService, never()).loadUserByUsername("admin@example.com");
        verify(chain).doFilter(request, response);
    }
}
