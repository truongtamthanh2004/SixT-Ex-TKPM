package com.example.sixt.repositories;

import com.example.sixt.models.StudentStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentStatusRepository extends JpaRepository<StudentStatusEntity, Long> {
    StudentStatusEntity findByName(String name);
}
