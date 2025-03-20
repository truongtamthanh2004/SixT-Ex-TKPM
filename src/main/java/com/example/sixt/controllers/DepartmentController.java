package com.example.sixt.controllers;

import com.example.sixt.controllers.requests.DepartmentCreationRequest;
import com.example.sixt.controllers.requests.ProgramCreationRequest;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.DepartmentEntity;
import com.example.sixt.models.ProgramEntity;
import com.example.sixt.services.DepartmentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    @Autowired
    public DepartmentController(DepartmentService departmentService, ObjectMapper objectMapper) {
        this.departmentService = departmentService;
        this.objectMapper = objectMapper;
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

    // Import CSV
    @PostMapping(value = "/import/csv", consumes = "multipart/form-data")
    public Map<String, Object> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            departmentService.importCsv(file);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "200");
            response.put("message", "Import CSV successfully!");
            response.put("data", 1);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
    }

    // Export CSV
    @GetMapping(value = "/export/csv", produces = "text/csv;charset=ISO-8859-1")
    public Map<String, Object> exportCsv(HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=departments.csv");
            departmentService.exportCsv(response);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "200");
            responseBody.put("message", "Export CSV successfully!");
            responseBody.put("data", 1);

            return responseBody;
        }
        catch (IOException e) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "500");
            responseBody.put("message", e.getMessage());
            responseBody.put("data", 0);

            return responseBody;
        }
    }

    @PostMapping(value = "/import/json", consumes = "multipart/form-data")
    public Map<String, Object> importJson(
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DepartmentEntity> departments = objectMapper.readValue(file.getInputStream(), new TypeReference<List<DepartmentEntity>>() {});

            departmentService.saveAll(departments);

            response.put("status", "200");
            response.put("message", "Import JSON successfully!");
            response.put("data", departments.size());
        } catch (Exception e) {
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
        }
        return response;
    }

    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportJson() {
        try {
            List<DepartmentEntity> departments = departmentService.getAllDepartments();
            ObjectMapper objectMapper = new ObjectMapper();

            byte[] jsonData = objectMapper.writeValueAsBytes(departments);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=departments.json");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

            return new ResponseEntity<>(jsonData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
