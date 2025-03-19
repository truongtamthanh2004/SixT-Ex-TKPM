package com.example.sixt.controllers.requests;

import com.example.sixt.commons.Department;
import com.example.sixt.commons.Gender;
import com.example.sixt.commons.StudentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class StudentUpdateRequest implements Serializable {
    private String fullName;
    private Date birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String department;

    private String course;
    private String program;

    @Pattern(regexp = "^(0[3-9][0-9]{8}|\\+84[3-9][0-9]{8})$", message = "Invalid phone number format")
    private String phoneNumber;

    private String status;

    private List<AddressRequest> addresses;
    private IdentityDocumentRequest identityDocument;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AddressRequest> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressRequest> addresses) {
        this.addresses = addresses;
    }

    public IdentityDocumentRequest getIdentityDocument() {
        return identityDocument;
    }

    public void setIdentityDocument(IdentityDocumentRequest identityDocuments) {
        this.identityDocument = identityDocuments;
    }
}
