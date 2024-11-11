package com.finsightanalytics.recommendationengine.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("FinancialGoal")
public class Neo4jFinancialGoal {

    @Id
    private Long financialGoalId; // Maps to SQL Financial Goal ID
    private Long userId;

    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate lastContributionDate;

    private boolean achieved = false;

    public Neo4jFinancialGoal() {}

    public Neo4jFinancialGoal(Long financialGoalId, Long userId, String name, BigDecimal targetAmount, BigDecimal currentAmount, LocalDate startDate, LocalDate endDate, boolean achieved) {
        this.financialGoalId = financialGoalId;
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.achieved = achieved;
    }

    // Getters and Setters
    public Long getFinancialGoalId() {
        return financialGoalId;
    }

    public void setfinancialGoalId(Long financialGoalId) {
        this.financialGoalId = financialGoalId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
