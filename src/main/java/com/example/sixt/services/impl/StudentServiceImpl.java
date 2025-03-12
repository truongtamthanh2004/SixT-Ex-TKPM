package com.example.sixt.services.impl;

import com.example.sixt.commons.StudentStatus;
import com.example.sixt.controllers.requests.StudentCreationRequest;
import com.example.sixt.controllers.requests.StudentUpdateRequest;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.StudentEntity;
import com.example.sixt.repositories.StudentRepository;
import com.example.sixt.services.StudentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository, ModelMapper modelMapper) {
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public StudentEntity addStudent(StudentCreationRequest student) {
        if (studentRepository.findByStudentId(student.getStudentId()) != null) {
            throw new InvalidDataException("Student id already exists.");
        }

        if (studentRepository.findByEmail(student.getEmail()) != null) {
            throw new InvalidDataException("Email already exists.");
        }

        StudentEntity studentEntity = modelMapper.map(student, StudentEntity.class);
        studentEntity.setId(null);

        return studentRepository.save(studentEntity);
    }

    @Override
    public void deleteStudent(String studentId) {
        StudentEntity student = studentRepository.findByStudentId(studentId);
        if (student != null) {
            studentRepository.delete(student);
        }
        else {
            throw new InvalidDataException("Student not found.");
        }
    }

    @Override
    @Transactional
    public StudentEntity updateStudent(String studentId, StudentUpdateRequest updatedStudent) {
        if (studentRepository.findByStudentId(studentId) == null) {
            throw new InvalidDataException("Student not found.");
        }

        StudentEntity existingStudent = studentRepository.findByStudentId(studentId);
        if (existingStudent != null) {
            if (updatedStudent.getFullName() != null) {
                existingStudent.setFullName(updatedStudent.getFullName());
            }
            if (updatedStudent.getBirthday() != null) {
                existingStudent.setBirthday(updatedStudent.getBirthday());
            }
            if (updatedStudent.getGender() != null) {
                existingStudent.setGender(updatedStudent.getGender());
            }
            if (updatedStudent.getDepartment() != null) {
                existingStudent.setDepartment(updatedStudent.getDepartment());
            }
            if (updatedStudent.getCourse() != null) {
                existingStudent.setCourse(updatedStudent.getCourse());
            }
            if (updatedStudent.getProgram() != null) {
                existingStudent.setProgram(updatedStudent.getProgram());
            }
            if (updatedStudent.getAddress() != null) {
                existingStudent.setAddress(updatedStudent.getAddress());
            }
            if (updatedStudent.getPhoneNumber() != null) {
                existingStudent.setPhoneNumber(updatedStudent.getPhoneNumber());
            }
            if (updatedStudent.getStatus() != null) {
                existingStudent.setStatus(updatedStudent.getStatus());
            }
            return studentRepository.save(existingStudent);
        }
        return null;
    }

    @Override
    public List<StudentEntity> searchStudents(String keyword) {
        return studentRepository.findByStudentIdOrFullName(keyword);
    }
}
