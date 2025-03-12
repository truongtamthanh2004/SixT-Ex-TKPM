package com.example.sixt.controllers.requests;

import com.example.sixt.commons.Department;
import com.example.sixt.commons.Gender;
import com.example.sixt.commons.StudentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Date;

public class StudentCreationRequest implements Serializable {
    @NotBlank(message = "Student id must be not blank")
    private String studentId;

    @NotBlank(message = "First name must be not blank")
    private String fullName;

    private Date birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Department department;

    private String course;
    private String program;
    private String address;

    @Email(message = "Email invalid")
    private String email;

    @Pattern(regexp = "^(0[3-9][0-9]{8}|\\+84[3-9][0-9]{8})$", message = "Invalid phone number format")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private StudentStatus status;

    public @NotBlank(message = "Student id must be not blank") String getStudentId() {
        return studentId;
    }

    public void setStudentId(@NotBlank(message = "Student id must be not blank") String studentId) {
        this.studentId = studentId;
    }

    public @NotBlank(message = "First name must be not blank") String getFullName() {
        return fullName;
    }

    public void setFullName(@NotBlank(message = "First name must be not blank") String fullName) {
        this.fullName = fullName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public @Email(message = "Email invalid") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "Email invalid") String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }
}
