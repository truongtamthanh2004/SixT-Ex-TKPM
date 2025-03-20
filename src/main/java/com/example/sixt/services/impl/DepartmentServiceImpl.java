package com.example.sixt.services.impl;

import com.example.sixt.controllers.requests.DepartmentCreationRequest;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.DepartmentEntity;
import com.example.sixt.repositories.DepartmentRepository;
import com.example.sixt.services.DepartmentService;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;
    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                              ModelMapper modelMapper) {
        this.departmentRepository = departmentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public DepartmentEntity updateDepartment(Long id, String department) {
        try {
            DepartmentEntity departmentEntity = departmentRepository.findById(id).orElseThrow(() -> new InvalidDataException("Student status not found"));
            departmentEntity.setName(department);
            DepartmentEntity savedDepartment = departmentRepository.save(departmentEntity);
            log.info("Program updated: {}", savedDepartment);
            return savedDepartment;
        }
        catch (InvalidDataException e) {
            log.error("Error updating Program: {}", e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.error("Error updating program: {}", e.getMessage());
            throw new RuntimeException("Program not found");
        }
    }

    @Override
    public DepartmentEntity addDepartment(DepartmentCreationRequest department) {
        try {
            if (departmentRepository.findByName(department.getName()) != null) {
                throw new InvalidDataException("Department already exists");
            }
            DepartmentEntity departmentEntity = modelMapper.map(department, DepartmentEntity.class);

            DepartmentEntity savedDepartment = departmentRepository.save(departmentEntity);
            log.info("Department added: {}", savedDepartment);

            return savedDepartment;
        }
        catch (InvalidDataException e) {
            log.error("Error adding department: {}", e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.error("Error adding department: {}", e.getMessage());
            throw new RuntimeException("Error adding department");
        }
    }

    @Override
    public DepartmentEntity getDepartmentById(Long id) {
        log.info("Getting Department by id: {}", id);
        return departmentRepository.findById(id).orElseThrow(() -> new InvalidDataException("Department not found"));
    }

    @Override
    public void importCsv(MultipartFile file) throws Exception {
        List<DepartmentEntity> departments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] data = line.split(",");
                if (data.length >= 2) {
                    DepartmentEntity department = new DepartmentEntity();
                    department.setName(data[1]);
                    departments.add(department);
                }
            }
        }
        departmentRepository.saveAll(departments);
    }

    @Override
    public void exportCsv(HttpServletResponse response) throws IOException {
        List<DepartmentEntity> departments = departmentRepository.findAll();
        PrintWriter writer = response.getWriter();
        writer.println("ID,Name");

        for (DepartmentEntity department : departments) {
            writer.println(department.getId() + "," + department.getName());
        }

        writer.flush();
        writer.close();
    }

    public void saveAll(List<DepartmentEntity> departments) {
        departmentRepository.saveAll(departments);
    }

    public List<DepartmentEntity> getAllDepartments() {
        return departmentRepository.findAll();
    }
}
