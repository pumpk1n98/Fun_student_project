package com.example.student.configuration;

import com.example.student.model.Role;
import com.example.student.model.Student;
import com.example.student.model.User;
import com.example.student.repository.StudentRepository;
import com.example.student.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppInitializerTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final StudentRepository studentRepository = mock(StudentRepository.class);

    @Test
    void createsDemoUsersAndStudentsWhenDatabaseIsEmpty() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));
        when(studentRepository.count()).thenReturn(0L);
        AppInitializer initializer = new AppInitializer(userRepository, passwordEncoder, studentRepository);

        initializer.init();

        verify(userRepository, times(3)).save(any(User.class));
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(
                user -> user.getEmail().equals("admin@example.com")
                        && user.getPassword().equals("encoded-admin")
                        && user.getRoles().equals(Set.of(Role.ADMIN))));
        verify(studentRepository, times(5)).save(any(Student.class));
    }

    @Test
    void skipsExistingUsersAndStudents() {
        when(userRepository.existsByEmail(any())).thenReturn(true);
        when(studentRepository.count()).thenReturn(5L);
        AppInitializer initializer = new AppInitializer(userRepository, passwordEncoder, studentRepository);

        initializer.init();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
        verify(studentRepository, never()).save(any(Student.class));
    }
}
