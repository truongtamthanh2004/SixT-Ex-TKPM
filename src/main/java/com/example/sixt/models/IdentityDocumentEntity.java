package com.example.sixt.models;

import com.example.sixt.commons.IdentityType;
import jakarta.persistence.*;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "identity_documents")
@Getter
@Setter
public class IdentityDocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Enumerated(EnumType.STRING)
    private IdentityType type;  // CMND, CCCD, PASSPORT

    private String number;
    private Date issueDate;
    private String issuePlace;
    private Date expiryDate;

    private Boolean hasChip;
    private String country;
    private String note;

}

