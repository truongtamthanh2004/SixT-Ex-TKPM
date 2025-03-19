package com.example.sixt.controllers.requests;

import com.example.sixt.commons.IdentityType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public class IdentityDocumentRequest {
    @Enumerated(EnumType.STRING)
    @NotBlank(message = "Identity type must be not blank")
    private IdentityType type;

    @NotBlank(message = "Identity number must be not blank")
    private String number;

    private Date issueDate;
    private String issuePlace;
    private Date expiryDate;
    private Boolean hasChip;
    private String country;
    private String note;

    // Getters & Setters
    public IdentityType getType() {
        return type;
    }

    public void setType(IdentityType type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public String getIssuePlace() {
        return issuePlace;
    }

    public void setIssuePlace(String issuePlace) {
        this.issuePlace = issuePlace;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getHasChip() {
        return hasChip;
    }

    public void setHasChip(Boolean hasChip) {
        this.hasChip = hasChip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
