package com.finsightanalytics.transactionservice.service;

import com.finsightanalytics.common.model.Transaction;

import java.util.List;

public interface TransactionService {

    List<Transaction> getAllTransactions();

    Transaction getTransactionById(Long id);

    Transaction createTransaction(Transaction transaction);

    Transaction updateTransaction(Long id, Transaction transaction);

    void deleteTransaction(Long id);

    List<Transaction> getTransactionsByUserId(Long userId);
}
