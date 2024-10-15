package com.finsightanalytics.common.model;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public class AnalyticsData {

    private double totalAmountSpent;
    private int transactionCount;
    private double averageTransactionAmount;
    private int creditCount;
    private int debitCount;
    private LocalDateTime lastTransactionDate;

    // Getters and setters

    public double getTotalAmountSpent() {
        return totalAmountSpent;
    }

    public void setTotalAmountSpent(double totalAmountSpent) {
        this.totalAmountSpent = totalAmountSpent;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public double getAverageTransactionAmount() {
        return averageTransactionAmount;
    }

    public void setAverageTransactionAmount(double averageTransactionAmount) {
        this.averageTransactionAmount = averageTransactionAmount;
    }

    public int getCreditCount() {
        return creditCount;
    }

    public void setCreditCount(int creditCount) {
        this.creditCount = creditCount;
    }

    public int getDebitCount() {
        return debitCount;
    }

    public void setDebitCount(int debitCount) {
        this.debitCount = debitCount;
    }

    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
}
