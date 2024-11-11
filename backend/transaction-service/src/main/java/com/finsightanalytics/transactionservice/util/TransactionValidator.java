package com.finsightanalytics.transactionservice.util;

import com.finsightanalytics.common.exception.InvalidTransactionException;
import com.finsightanalytics.common.model.Transaction;
import com.finsightanalytics.common.model.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionValidator {

    public void validateTransaction(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be greater than zero");
        }

        if (transaction.getType() == null || (transaction.getType() != TransactionType.CREDIT && transaction.getType() != TransactionType.DEBIT)) {
            throw new InvalidTransactionException("Transaction type must be 'credit' or 'debit'");
        }
    }
}
