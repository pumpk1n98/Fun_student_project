package com.example.student.controller;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.model.Role;
import com.example.student.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {
    private final UserService userService = mock(UserService.class);
    private final UserController controller = new UserController(userService);

    @Test
    void delegatesUserOperations() {
        UserRequest request = new UserRequest();
        request.setEmail("admin@example.com");
        request.setRoles(Set.of(Role.ADMIN));
        UserResponse user = new UserResponse(1L, request.getEmail(), request.getRoles());
        UserResponseList users = new UserResponseList(List.of(user), 1);
        when(userService.findAll()).thenReturn(users);
        when(userService.save(request)).thenReturn(user);
        when(userService.updateUser(request)).thenReturn(user);

        assertEquals(users, controller.getAllUsers());
        assertEquals(user, controller.save(request));
        assertEquals(user, controller.updateUser(request));
        controller.deleteById(1L);
        verify(userService).deleteById(1L);
    }
}
