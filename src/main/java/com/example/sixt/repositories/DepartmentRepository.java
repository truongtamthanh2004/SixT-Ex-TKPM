package com.example.sixt.repositories;

import com.example.sixt.models.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

  DepartmentEntity findByName(String name);
}
