package com.finsightanalytics.recommendationengine.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.finsightanalytics.common.model.TransactionType;

@Node("Transaction")
public class Neo4jTransaction {

    @Id
    private Long transactionId; // Maps to SQL transaction ID for reference
    private Long userId;
    private Long financialGoalId;
    private Long debtId;

    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime timestamp;
    

    public Neo4jTransaction() {}

    public Neo4jTransaction(Long transactionId, Long userId, Long financialGoalId, Long debtId, BigDecimal amount, TransactionType type, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.financialGoalId = financialGoalId;
        this.debtId = debtId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFinancialGoalId() {
        return financialGoalId;
    }

    public void setFinancialGoalId(Long financialGoalId) {
        this.financialGoalId = financialGoalId;
    }

    public Long getDebtId() {
        return debtId;
    }

    public void setDebtId(Long debtId) {
        this.debtId = debtId;
    }
}
