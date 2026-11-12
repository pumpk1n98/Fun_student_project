package com.example.student.service.impl;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.exception.EmailAlreadyExistsException;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.mapper.UserMapperImpl;
import com.example.student.model.Role;
import com.example.student.model.User;
import com.example.student.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UserServiceImpl(userRepository, new UserMapperImpl(), passwordEncoder);
    }

    @Test
    void checksWhetherEmailExists() {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

        assertTrue(service.existsByEmail("admin@example.com"));
    }

    @Test
    void registerEncodesPasswordAndSavesNewUser() {
        UserRequest request = request("new@example.com", "plain");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");

        assertTrue(service.register(request));

        verify(passwordEncoder).encode("plain");
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(
                user -> user.getEmail().equals("new@example.com") && user.getPassword().equals("encoded")));
    }

    @Test
    void registerRejectsDuplicateEmailAndMissingEmail() {
        UserRequest duplicate = request("admin@example.com", "plain");
        when(userRepository.existsByEmail(duplicate.getEmail())).thenReturn(true);
        assertFalse(service.register(duplicate));

        UserRequest missingEmail = request(null, "plain");
        assertFalse(service.register(missingEmail));
    }

    @Test
    void registerAllowsBlankPasswordWithoutEncoding() {
        UserRequest request = request("new@example.com", " ");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        assertTrue(service.register(request));

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user -> " ".equals(user.getPassword())));
    }

    @Test
    void findsUserByEmailOrThrows() {
        User user = user(2L, "admin@example.com");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        assertEquals(user, service.findByEmail(user.getEmail()));

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.findByEmail("missing@example.com"));
    }

    @Test
    void listsUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user(2L, "admin@example.com")));

        UserResponseList response = service.findAll();

        assertEquals(1, response.getTotalCount());
        assertEquals("admin@example.com", response.getResponses().getFirst().getEmail());
    }

    @Test
    void savesUserAndRejectsDuplicateEmail() {
        UserRequest request = request("new@example.com", "plain");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> {
                    User saved = invocation.getArgument(0);
                    saved.setId(5L);
                    return saved;
                });

        UserResponse response = service.save(request);

        assertEquals(5L, response.getId());
        assertEquals("new@example.com", response.getEmail());
        assertEquals(Set.of(Role.ADMIN), response.getRoles());
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(
                user -> user.getPassword().equals("encoded")));

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> service.save(request));
    }

    @Test
    void updatesUserDetailsAndReturnsResponse() {
        User user = user(2L, "admin@example.com");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        UserRequest request = request("admin@example.com", "unused");
        request.setRoles(Set.of(Role.SUPER_ADMIN));

        UserResponse response = service.updateUser(request);

        assertEquals(2L, response.getId());
        assertEquals(Set.of(Role.SUPER_ADMIN), response.getRoles());
    }

    @Test
    void deletesUserById() {
        service.deleteById(2L);

        verify(userRepository).deleteById(2L);
    }

    private UserRequest request(String email, String password) {
        UserRequest request = new UserRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setRoles(Set.of(Role.ADMIN));
        return request;
    }

    private User user(Long id, String email) {
        return User.builder().id(id).email(email).password("encoded").roles(Set.of(Role.ADMIN)).build();
    }
}
