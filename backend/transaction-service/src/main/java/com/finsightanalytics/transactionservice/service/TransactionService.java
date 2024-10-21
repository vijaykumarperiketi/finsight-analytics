package com.finsightanalytics.transactionservice.service;

import com.finsightanalytics.common.model.Transaction;

import java.util.List;

public interface TransactionService {

    List<Transaction> getAllTransactions();

    Transaction getTransactionById(Long id);

    Transaction createTransaction(Transaction transaction);

    List<Transaction> getTransactionsByUserId(Long userId);
}
