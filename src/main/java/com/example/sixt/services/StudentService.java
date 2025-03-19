package com.example.sixt.services;

import com.example.sixt.controllers.requests.StudentCreationRequest;
import com.example.sixt.controllers.requests.StudentUpdateRequest;
import com.example.sixt.controllers.responses.StudentResponse;
import com.example.sixt.models.StudentEntity;

import java.util.List;

public interface StudentService {
    StudentResponse addStudent(StudentCreationRequest student);
    void deleteStudent(String studentId);
    StudentResponse updateStudent(String studentId, StudentUpdateRequest student);
    List<StudentResponse> searchStudents(String keyword);
    List<StudentResponse> searchStudentsByDepartmentAndName(String keyword, String department);
}
