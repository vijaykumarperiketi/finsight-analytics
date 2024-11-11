package com.finsightanalytics.common.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long recommendationId;

    private Long userId;
    private String type;
    private String message;
    private LocalDate dateCreated;
    private Long bankerId;

    @PrePersist
    public void prePersist() {
        dateCreated = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        dateCreated = LocalDate.now();
    }

    // Getters and setters
    public Long getRecommendationId() {
        return recommendationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Long getBankerId() {
        return bankerId;
    }

    public void setBankerId(Long bankerId) {
        this.bankerId = bankerId;
    }
}
