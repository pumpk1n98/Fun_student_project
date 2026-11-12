package com.example.student.controller;

import com.example.student.dto.StudentFullResponse;
import com.example.student.dto.StudentRequest;
import com.example.student.dto.StudentResponse;
import com.example.student.dto.StudentResponseList;
import com.example.student.dto.StudentUpdateRequest;
import com.example.student.service.StudentService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentControllerTest {
    private final StudentService studentService = mock(StudentService.class);
    private final StudentController controller = new StudentController(studentService);

    @Test
    void delegatesStudentReads() {
        StudentResponseList list = new StudentResponseList(List.of(), 0);
        StudentFullResponse student = new StudentFullResponse(3L, "Anna", "Ivanova", null, null);
        when(studentService.findAll()).thenReturn(list);
        when(studentService.findById(3L)).thenReturn(student);

        assertEquals(list, controller.findAll());
        assertEquals(student, controller.findStudentById(3L));
        assertEquals(student, controller.getFullInfoById(3L));
        verify(studentService, org.mockito.Mockito.times(2)).findById(3L);
    }

    @Test
    void delegatesStudentWrites() {
        StudentRequest create = new StudentRequest();
        StudentUpdateRequest update = new StudentUpdateRequest();
        StudentResponse response = new StudentResponse(3L, "Anna", "Ivanova");
        when(studentService.save(create)).thenReturn(response);
        when(studentService.update(3L, update)).thenReturn(response);

        assertEquals(response, controller.createStudent(create));
        assertEquals(response, controller.updateStudent(3L, update));
        controller.deleteStudentById(3L);
        verify(studentService).deleteById(3L);
    }
}
