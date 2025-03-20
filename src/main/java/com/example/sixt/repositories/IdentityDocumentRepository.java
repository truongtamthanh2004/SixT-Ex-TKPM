package com.example.sixt.repositories;

import com.example.sixt.models.IdentityDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentityDocumentRepository extends JpaRepository<IdentityDocumentEntity, Long> {
    IdentityDocumentEntity findByStudentId(String studentId);
    void deleteByStudentId(String studentId);
}
