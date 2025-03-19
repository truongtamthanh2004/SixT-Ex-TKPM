package com.example.sixt.services.impl;

import com.example.sixt.controllers.requests.ProgramCreationRequest;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.ProgramEntity;
import com.example.sixt.repositories.ProgramRepository;
import com.example.sixt.services.ProgramService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProgramServiceImpl implements ProgramService {
    private final ProgramRepository programRepository;
    private final ModelMapper modelMapper;
    private static final Logger log = LoggerFactory.getLogger(ProgramServiceImpl.class);

    @Autowired
    public ProgramServiceImpl(ProgramRepository programRepository,
                                    ModelMapper modelMapper) {
        this.programRepository = programRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProgramEntity addProgram(ProgramCreationRequest program) {
        try {
            if (programRepository.findByName(program.getName()) != null) {
                throw new InvalidDataException("Student status already exists");
            }
            ProgramEntity studentStatusEntity = modelMapper.map(program, ProgramEntity.class);

            ProgramEntity savedStatus = programRepository.save(studentStatusEntity);
            log.info("Program added: {}", savedStatus);

            return savedStatus;
        }
        catch (InvalidDataException e) {
            log.error("Error adding program: {}", e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.error("Error adding program: {}", e.getMessage());
            throw new RuntimeException("Error adding program");
        }
    }

    @Override
    public ProgramEntity getProgramById(Long id) {
        log.info("Getting program by id: {}", id);
        return programRepository.findById(id).orElseThrow(() -> new InvalidDataException("Program not found"));
    }

    @Override
    public ProgramEntity updateProgram(Long id, String name) {
        try {
            ProgramEntity programEntity = programRepository.findById(id).orElseThrow(() -> new InvalidDataException("Student status not found"));
            programEntity.setName(name);
            ProgramEntity savedProgram = programRepository.save(programEntity);
            log.info("Program updated: {}", savedProgram);
            return savedProgram;
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
}
