package com.finsightanalytics.transactionservice.util;

import com.finsightanalytics.common.exception.InvalidTransactionException;
import com.finsightanalytics.common.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionValidator {

    public void validateTransaction(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be greater than zero");
        }

        if (transaction.getType() == null || (!"credit".equalsIgnoreCase(transaction.getType()) && !"debit".equalsIgnoreCase(transaction.getType()))) {
            throw new InvalidTransactionException("Transaction type must be 'credit' or 'debit'");
        }
    }
}
