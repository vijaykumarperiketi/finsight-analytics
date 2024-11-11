package com.finsightanalytics.recommendationengine.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Debt")
public class Neo4jDebt {

    @Id
    private Long debtId;
    private Long userId;

    private String name;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate lastContributionDate;

    private boolean cleared = false;

    public Neo4jDebt(Long debtId, Long userId, String name, BigDecimal totalAmount, BigDecimal paidAmount, LocalDate startDate, LocalDate endDate, LocalDate lastContributionDate, boolean cleared) {
        this.debtId = debtId;
        this.name = name;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.lastContributionDate = lastContributionDate;
        this.cleared = cleared;
    }

    // Getters and setters
    public Long getDebtId() { return debtId; }
    public void setDebtId(Long debtId) { this.debtId = debtId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public boolean isCleared() { return cleared; }
    public void setCleared(boolean cleared) { this.cleared = cleared; }

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
