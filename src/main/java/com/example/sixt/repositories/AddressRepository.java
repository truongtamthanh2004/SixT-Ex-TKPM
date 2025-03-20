package com.example.sixt.repositories;

import com.example.sixt.models.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findAllByStudentId(String studentId);
    void deleteAllByStudentId(String studentId);
}
