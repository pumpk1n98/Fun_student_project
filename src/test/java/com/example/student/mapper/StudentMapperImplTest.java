package com.example.student.mapper;

import com.example.student.dto.StudentFullResponse;
import com.example.student.dto.StudentRequest;
import com.example.student.dto.StudentResponse;
import com.example.student.dto.StudentResponseForList;
import com.example.student.dto.StudentResponseList;
import com.example.student.model.Student;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentMapperImplTest {
    private final StudentMapperImpl mapper = new StudentMapperImpl();

    @Test
    void mapsRequestToStudent() {
        StudentRequest request = new StudentRequest();
        request.setName("Anna");
        request.setSurname("Ivanova");
        request.setEmail("anna@example.com");
        request.setPhoneNumber("+100000000");

        Student student = mapper.requestToStudent(request);

        assertEquals("Anna", student.getName());
        assertEquals("Ivanova", student.getSurname());
        assertEquals("anna@example.com", student.getEmail());
        assertEquals("+100000000", student.getPhoneNumber());
    }

    @Test
    void mapsStudentToFullResponse() {
        StudentFullResponse response = mapper.studentToFullResponse(student());

        assertEquals(7L, response.getId());
        assertEquals("Anna", response.getName());
        assertEquals("Ivanova", response.getSurname());
        assertEquals("anna@example.com", response.getEmail());
        assertEquals("+100000000", response.getPhoneNumber());
    }

    @Test
    void mapsStudentToSummaryResponse() {
        StudentResponse response = mapper.studentToResponse(student());

        assertEquals(7L, response.getId());
        assertEquals("Anna", response.getName());
        assertEquals("Ivanova", response.getSurname());
    }

    @Test
    void mapsStudentListsAndCountsThem() {
        Student student = student();

        StudentResponseForList summary = mapper.studentToStudentResponseForList(student);
        StudentResponseList response = mapper.studentListToStudentResponseList(List.of(student));

        assertEquals(7L, summary.getId());
        assertEquals("anna@example.com", summary.getEmail());
        assertEquals("+100000000", summary.getPhoneNumber());
        assertEquals(1, response.getTotalCount());
        assertEquals("Anna", response.getResponse().getFirst().getName());
        assertEquals(1, mapper.studentListToListOfStudentResponse(List.of(student)).size());
    }

    private Student student() {
        Student student = new Student();
        student.setId(7L);
        student.setName("Anna");
        student.setSurname("Ivanova");
        student.setEmail("anna@example.com");
        student.setPhoneNumber("+100000000");
        return student;
    }
}
