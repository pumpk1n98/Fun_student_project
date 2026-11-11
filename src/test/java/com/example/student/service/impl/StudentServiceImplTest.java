package com.example.student.service.impl;

import com.example.student.dto.StudentFullResponse;
import com.example.student.dto.StudentResponseList;
import com.example.student.dto.StudentUpdateRequest;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.mapper.StudentMapperImpl;
import com.example.student.model.Student;
import com.example.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentServiceImplTest {
    private StudentRepository studentRepository;
    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        studentService = new StudentServiceImpl(studentRepository, new StudentMapperImpl());
    }

    @Test
    void findByIdReturnsStudentDetails() {
        Student student = student(7L, "Anna", "Ivanova", "anna@example.com", "+100000000");
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));

        StudentFullResponse response = studentService.findById(7L);

        assertEquals(7L, response.getId());
        assertEquals("Anna", response.getName());
        assertEquals("Ivanova", response.getSurname());
        assertEquals("anna@example.com", response.getEmail());
        assertEquals("+100000000", response.getPhoneNumber());
    }

    @Test
    void findByIdThrowsWhenStudentDoesNotExist() {
        when(studentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> studentService.findById(404L));
    }

    @Test
    void findAllIncludesContactDetailsForTheFrontend() {
        when(studentRepository.findAll()).thenReturn(List.of(
                student(7L, "Anna", "Ivanova", "anna@example.com", "+100000000")
        ));

        StudentResponseList response = studentService.findAll();

        assertEquals(1, response.getTotalCount());
        assertEquals("anna@example.com", response.getResponse().getFirst().getEmail());
        assertEquals("+100000000", response.getResponse().getFirst().getPhoneNumber());
    }

    @Test
    void updateChangesProvidedFieldsAndKeepsExistingValues() {
        Student student = student(7L, "Anna", "Ivanova", "old@example.com", "+100000000");
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        studentService.update(7L, new StudentUpdateRequest(null, "new@example.com"));

        assertEquals("new@example.com", student.getEmail());
        assertEquals("+100000000", student.getPhoneNumber());
        verify(studentRepository).save(student);
    }

    private Student student(Long id, String name, String surname, String email, String phoneNumber) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setSurname(surname);
        student.setEmail(email);
        student.setPhoneNumber(phoneNumber);
        return student;
    }
}
