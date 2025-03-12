package com.example.sixt.services;

import com.example.sixt.controllers.requests.StudentCreationRequest;
import com.example.sixt.controllers.requests.StudentUpdateRequest;
import com.example.sixt.models.StudentEntity;

import java.util.List;

public interface StudentService {
    StudentEntity addStudent(StudentCreationRequest student);
    void deleteStudent(String studentId);
    StudentEntity updateStudent(String studentId, StudentUpdateRequest student);
    List<StudentEntity> searchStudents(String keyword);
}
