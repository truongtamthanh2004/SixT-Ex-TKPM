package com.example.sixt.controllers;

import com.example.sixt.controllers.requests.StatusCreationRequest;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.StudentStatusEntity;
import com.example.sixt.services.StudentStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/student-statuses")
public class StudentStatusController {
    private final StudentStatusService studentStatusService;
    private static final Logger log = LoggerFactory.getLogger(StudentStatusController.class);

    public StudentStatusController(StudentStatusService studentStatusService) {
        this.studentStatusService = studentStatusService;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            log.info("Updating student status for id: {}", id);
            StudentStatusEntity updatedStatus = studentStatusService.updateStatus(id, status);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "201");
            response.put("message", "Student status updated successfully");
            response.put("data", updatedStatus);
            return response;
        }
        catch (InvalidDataException e) {
            log.error("Error updating student status: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "409");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
        catch (Exception e) {
            log.error("Error updating student status: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
    }

    @PostMapping
    public Map<String, Object> addStatus(@RequestBody StatusCreationRequest statusRequest) {
        try {
            log.info("Adding new student status: {}", statusRequest.getName());
            StudentStatusEntity newStatus = studentStatusService.addStatus(statusRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "201");
            response.put("message", "Student status added successfully");
            response.put("data", newStatus);
            return response;
        }
        catch (InvalidDataException e) {
            log.error("Error adding student status: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "409");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
        catch (Exception e) {
            log.error("Error adding student status: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
    }

    @GetMapping("/{id}")
    public Map<String, Object> getStatusById(@PathVariable Long id) {
        try {
            log.info("Fetching student status by id: {}", id);
            StudentStatusEntity status = studentStatusService.getStatusById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "200");
            response.put("message", "Student status fetched successfully");
            response.put("data", status);
            return response;
        }
        catch (InvalidDataException e) {
            log.error("Error fetching student status: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "409");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
        catch (Exception e) {
            log.error("Error fetching student status: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
    }
}
