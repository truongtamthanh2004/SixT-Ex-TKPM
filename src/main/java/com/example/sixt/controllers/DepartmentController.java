package com.example.sixt.controllers;

import com.example.sixt.controllers.requests.DepartmentCreationRequest;
import com.example.sixt.controllers.requests.ProgramCreationRequest;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.DepartmentEntity;
import com.example.sixt.models.ProgramEntity;
import com.example.sixt.services.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateProgram(@PathVariable Long id, @RequestParam String department) {
        try {
            log.info("Updating department for id: {}", id);
            DepartmentEntity updatedDepartment = departmentService.updateDepartment(id, department);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "201");
            response.put("message", "Department updated successfully");
            response.put("data", updatedDepartment);
            return response;
        }
        catch (InvalidDataException e) {
            log.error("Error updating department: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "409");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
        catch (Exception e) {
            log.error("Error updating department: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
    }

    @PostMapping
    public Map<String, Object> addProgram(@RequestBody DepartmentCreationRequest departmentCreationRequest) {
        try {
            log.info("Adding new Department: {}", departmentCreationRequest.getName());
            DepartmentEntity newDepartment = departmentService.addDepartment(departmentCreationRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "201");
            response.put("message", "Department added successfully");
            response.put("data", newDepartment);
            return response;
        }
        catch (InvalidDataException e) {
            log.error("Error adding department: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "409");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
        catch (Exception e) {
            log.error("Error adding department: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
    }

    @GetMapping("/{id}")
    public Map<String, Object> getDepartmentById(@PathVariable Long id) {
        try {
            log.info("Fetching department by id: {}", id);
            DepartmentEntity departmentEntity = departmentService.getDepartmentById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "200");
            response.put("message", "Department fetched successfully");
            response.put("data", departmentEntity);
            return response;
        }
        catch (InvalidDataException e) {
            log.error("Error fetching department: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "409");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
        catch (Exception e) {
            log.error("Error fetching department: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
    }
}
