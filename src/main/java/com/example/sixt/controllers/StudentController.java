package com.example.sixt.controllers;

import com.example.sixt.controllers.requests.StudentCreationRequest;
import com.example.sixt.controllers.requests.StudentUpdateRequest;
import com.example.sixt.controllers.responses.StudentResponse;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.StudentEntity;
import com.example.sixt.services.StudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Student Controller")
@Validated
public class StudentController {
    private final StudentService studentService;
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/add")
    public Map<String, Object> addStudent(@RequestBody @Valid StudentCreationRequest student) {
        try {
            StudentResponse studentEntity = studentService.addStudent(student);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.CREATED.value());
            response.put("message", "Student added successfully");
            response.put("data", studentEntity);

            log.info("Student added successfully");

            return response;
        }
        catch (InvalidDataException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("message", e.getMessage());
            response.put("data", 0);

            log.error(e.getMessage());

            return response;
        }
        catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Error adding a student");
            response.put("data", 0);

            log.error(e.getMessage());

            return response;
        }
    }

    @DeleteMapping("/delete/{studentId}")
    public Map<String, Object> deleteStudent(@PathVariable String studentId) {
        try {
            studentService.deleteStudent(studentId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Student deleted successfully");
            response.put("data", 1);

            log.info("Student deleted successfully");

            return response;
        }
        catch (InvalidDataException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("message", e.getMessage());
            response.put("data", 0);

            log.error(e.getMessage());

            return response;
        }
        catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Error adding a student");
            response.put("data", 0);

            log.error(e.getMessage());

            return response;
        }
    }

    @PatchMapping("/update/{studentId}")
    public Map<String, Object> updateStudent(@PathVariable String studentId, @RequestBody @Valid StudentUpdateRequest student) {
        try {
            StudentResponse studentResponse = studentService.updateStudent(studentId, student);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.CREATED.value());
            response.put("message", "Student updated successfully");
            response.put("data", studentResponse);

            log.info("Student updated successfully");

            return response;
        }
        catch (InvalidDataException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("message", e.getMessage());
            response.put("data", 0);

            log.error(e.getMessage());

            return response;
        }
        catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Error updating a student");
            response.put("data", 0);

            log.error(e.getMessage());

            return response;
        }
    }

    @GetMapping("/search/{keyword}")
    public Map<String, Object> searchStudents(@PathVariable String keyword) {
        try {
            List<StudentResponse> StudentResponses = studentService.searchStudents(keyword);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Found " + StudentResponses.size() + " students");
            response.put("data", StudentResponses);

            log.info("Found " + StudentResponses.size() + " students");

            return response;
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", e.getMessage());
            response.put("data", 0);

            log.error(e.getMessage());

            return response;
        }
    }
}
