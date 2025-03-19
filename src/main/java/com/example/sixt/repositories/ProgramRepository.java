package com.example.sixt.repositories;

import com.example.sixt.models.ProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<ProgramEntity, Long> {
    ProgramEntity findByName(String name);
}
