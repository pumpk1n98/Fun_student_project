package com.example.student.mapper;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.model.Role;
import com.example.student.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperImplTest {
    private final UserMapperImpl mapper = new UserMapperImpl();

    @Test
    void mapsRequestAndResponse() {
        UserRequest request = request();
        User user = mapper.toEntity(request);
        user.setId(3L);

        UserResponse response = mapper.toResponse(user);

        assertEquals("admin@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals(Set.of(Role.ADMIN), user.getRoles());
        assertEquals(3L, response.getId());
        assertEquals("admin@example.com", response.getEmail());
        assertEquals(Set.of(Role.ADMIN), response.getRoles());
    }

    @Test
    void mapsUserListAndCount() {
        User user = mapper.toEntity(request());
        user.setId(8L);
        UserResponseList response = mapper.toListResponse(List.of(user));

        assertEquals(1, response.getTotalCount());
        assertEquals("admin@example.com", response.getResponses().getFirst().getEmail());
        assertEquals(1, mapper.toResponseList(List.of(user)).size());
    }

    private UserRequest request() {
        UserRequest request = new UserRequest();
        request.setEmail("admin@example.com");
        request.setPassword("secret");
        request.setRoles(Set.of(Role.ADMIN));
        return request;
    }
}
