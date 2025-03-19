package com.example.sixt.services;

import com.example.sixt.controllers.requests.DepartmentCreationRequest;
import com.example.sixt.models.DepartmentEntity;

public interface DepartmentService {
    DepartmentEntity updateDepartment(Long id, String department);
    DepartmentEntity addDepartment(DepartmentCreationRequest department);
    DepartmentEntity getDepartmentById(Long id);
}
