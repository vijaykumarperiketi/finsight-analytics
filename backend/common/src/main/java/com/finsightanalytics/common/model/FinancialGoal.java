package com.finsightanalytics.common.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class FinancialGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long financialGoalId;
    private Long userId;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate lastContributionDate;
    private boolean achieved = false;

    // Getters and setters
    public Long getFinancialGoalId() {
        return financialGoalId;
    }

    public void setFinancialGoalId(Long financialGoalId) {
        this.financialGoalId = financialGoalId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }   

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }               

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }       

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }   

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
    }

    public LocalDate getLastContributionDate() {
        return lastContributionDate;
    }

    public void setLastContributionDate(LocalDate lastContributionDate) {
        this.lastContributionDate = lastContributionDate;
    }
}
