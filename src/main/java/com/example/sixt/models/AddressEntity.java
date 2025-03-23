package com.example.sixt.models;

import com.example.sixt.commons.AddressType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
public class AddressEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "student_id", nullable = false)
  private String studentId;

  @Enumerated(EnumType.STRING)
  private AddressType type;

  private String houseNumber;
  private String street;
  private String ward;
  private String district;
  private String province;
  private String country;

}

