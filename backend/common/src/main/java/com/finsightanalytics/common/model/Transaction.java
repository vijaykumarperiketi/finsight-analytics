package com.finsightanalytics.common.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.finsightanalytics.common.model.TransactionType;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private BigDecimal amount;
    private TransactionType type; // Could be "credit" or "debit"
    private LocalDateTime timestamp;

    private Long financialGoalId;
    private Long debtId;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getFinancialGoalId() {
        return financialGoalId;
    }

    public void setFinancialGoalId(String financialGoalId) {
        this.financialGoalId = financialGoalId;
    }

    public LocalDateTime getdebtId() {
        return debtId;
    }

    public void setDebtId(String debtId) {
        this.debtId = debtId;
    }
}
