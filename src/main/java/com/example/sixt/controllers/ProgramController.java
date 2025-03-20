package com.example.sixt.controllers;

import com.example.sixt.controllers.requests.ProgramCreationRequest;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.ProgramEntity;
import com.example.sixt.services.ProgramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/programs")
public class ProgramController {
    private final ProgramService programService;
    private static final Logger log = LoggerFactory.getLogger(ProgramController.class);

    @Autowired
    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateProgram(@PathVariable Long id, @RequestParam String program) {
        try {
            log.info("Updating program for id: {}", id);
            ProgramEntity updatedProgram = programService.updateProgram(id, program);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "201");
            response.put("message", "Program updated successfully");
            response.put("data", updatedProgram);
            return response;
        }
        catch (InvalidDataException e) {
            log.error("Error updating program: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "409");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
        catch (Exception e) {
            log.error("Error updating program: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
    }

    @PostMapping
    public Map<String, Object> addProgram(@RequestBody ProgramCreationRequest programCreationRequest) {
        try {
            log.info("Adding new program: {}", programCreationRequest.getName());
            ProgramEntity newProgram = programService.addProgram(programCreationRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "201");
            response.put("message", "Program added successfully");
            response.put("data", newProgram);
            return response;
        }
        catch (InvalidDataException e) {
            log.error("Error adding program: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "409");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
        catch (Exception e) {
            log.error("Error adding program: {}", e.getMessage());
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
            log.info("Fetching program by id: {}", id);
            ProgramEntity programEntity = programService.getProgramById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "200");
            response.put("message", "Program fetched successfully");
            response.put("data", programEntity);
            return response;
        }
        catch (InvalidDataException e) {
            log.error("Error fetching program: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "409");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
        catch (Exception e) {
            log.error("Error fetching program: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", e.getMessage());
            response.put("data", 0);
            return response;
        }
    }
}
