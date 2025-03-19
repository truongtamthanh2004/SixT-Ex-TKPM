package com.example.sixt.services;

import com.example.sixt.controllers.requests.StatusCreationRequest;
import com.example.sixt.models.StudentStatusEntity;

public interface StudentStatusService {
    StudentStatusEntity updateStatus(Long id, String status);
    StudentStatusEntity addStatus(StatusCreationRequest status);
    StudentStatusEntity getStatusById(Long id);
}
