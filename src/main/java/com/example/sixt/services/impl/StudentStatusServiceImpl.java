package com.example.sixt.services.impl;

import com.example.sixt.controllers.requests.StatusCreationRequest;
import com.example.sixt.exceptions.InvalidDataException;
import com.example.sixt.models.StudentStatusEntity;
import com.example.sixt.repositories.StudentStatusRepository;
import com.example.sixt.services.StudentStatusService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentStatusServiceImpl implements StudentStatusService {
    private final StudentStatusRepository studentStatusRepository;
    private final ModelMapper modelMapper;
    private static final Logger log = LoggerFactory.getLogger(StudentStatusServiceImpl.class);

    @Autowired
    public StudentStatusServiceImpl(StudentStatusRepository studentStatusRepository,
                                    ModelMapper modelMapper) {
        this.studentStatusRepository = studentStatusRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public StudentStatusEntity updateStatus(Long id, String status) {
        try {
            StudentStatusEntity studentStatusEntity = studentStatusRepository.findById(id).orElseThrow(() -> new InvalidDataException("Student status not found"));
            studentStatusEntity.setName(status);
            StudentStatusEntity savedStatus = studentStatusRepository.save(studentStatusEntity);
            log.info("Student status updated: {}", savedStatus);
            return savedStatus;
        }
        catch (InvalidDataException e) {
            log.error("Error updating student status: {}", e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.error("Error updating student status: {}", e.getMessage());
            throw new RuntimeException("Student status not found");
        }
    }

    @Override
    public StudentStatusEntity addStatus(StatusCreationRequest status) {
        try {
            if (studentStatusRepository.findByName(status.getName()) != null) {
                throw new InvalidDataException("Student status already exists");
            }
            StudentStatusEntity studentStatusEntity = modelMapper.map(status, StudentStatusEntity.class);

            StudentStatusEntity savedStatus = studentStatusRepository.save(studentStatusEntity);
            log.info("Student status added: {}", savedStatus);

            return savedStatus;
        }
        catch (InvalidDataException e) {
            log.error("Error adding student status: {}", e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.error("Error adding student status: {}", e.getMessage());
            throw new RuntimeException("Error adding student status");
        }
    }

    @Override
    public StudentStatusEntity getStatusById(Long id) {
        log.info("Getting student status by id: {}", id);
        return studentStatusRepository.findById(id).orElseThrow(() -> new InvalidDataException("Student status not found"));
    }
}
