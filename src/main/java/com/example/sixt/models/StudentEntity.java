package com.example.sixt.models;

import com.example.sixt.commons.Department;
import com.example.sixt.commons.Gender;
import com.example.sixt.commons.StudentStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "students",
    indexes = {
        @Index(name = "idx_student_id", columnList = "studentId"),
        @Index(name = "idx_email", columnList = "email")
    })
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class StudentEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "student_id", nullable = false, unique = true)
  private String studentId;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "date_of_birth")
  @Temporal(TemporalType.DATE)
  private Date birthday;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private Long department;

  private String course;
  private Long program;

  private String nationality;

  @Column(unique = true)
  private String email;

  @Column(name = "phone_number")
  private String phoneNumber;

  private Long status;

  @Column(name = "created_at", length = 255)
  @Temporal(TemporalType.TIMESTAMP)
  @CreationTimestamp
  private Date createdAt;

  @Column(name = "updated_at", length = 255)
  @Temporal(TemporalType.TIMESTAMP)
  @UpdateTimestamp
  private Date updatedAt;


}
