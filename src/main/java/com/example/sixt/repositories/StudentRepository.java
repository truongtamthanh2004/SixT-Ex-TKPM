package com.example.sixt.repositories;

import com.example.sixt.models.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    StudentEntity findByStudentId(String studentId);
    StudentEntity findByEmail(String email);

    @Query("SELECT s FROM StudentEntity s WHERE s.studentId LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<StudentEntity> findByStudentIdOrFullName(@Param("keyword") String keyword);
}
