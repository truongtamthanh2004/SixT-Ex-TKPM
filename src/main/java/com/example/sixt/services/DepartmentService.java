package com.example.sixt.services;

import com.example.sixt.controllers.requests.DepartmentCreationRequest;
import com.example.sixt.models.DepartmentEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DepartmentService {
    DepartmentEntity updateDepartment(Long id, String department);
    DepartmentEntity addDepartment(DepartmentCreationRequest department);
    DepartmentEntity getDepartmentById(Long id);
    public void exportCsv(HttpServletResponse response) throws IOException;
    public void importCsv(MultipartFile file) throws Exception;
    public void saveAll(List<DepartmentEntity> departments);
    public List<DepartmentEntity> getAllDepartments();
}
