package com.example.sixt.services;

import com.example.sixt.controllers.requests.ProgramCreationRequest;
import com.example.sixt.models.ProgramEntity;

public interface ProgramService {
    ProgramEntity updateProgram(Long id, String program);
    ProgramEntity addProgram(ProgramCreationRequest program);
    ProgramEntity getProgramById(Long id);
}
