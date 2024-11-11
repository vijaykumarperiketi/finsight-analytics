package com.finsightanalytics.transactionservice.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finsightanalytics.common.model.Transaction;
import com.finsightanalytics.transactionservice.repository.TransactionRepository;
import com.finsightanalytics.transactionservice.service.TransactionService;
import com.finsightanalytics.transactionservice.service.kafka.KafkaProducerService;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        Transaction savedTransaction = transactionRepository.save(transaction);
        kafkaProducerService.sendTransactionMessage(savedTransaction); // Notify other services via Kafka
        return savedTransaction;
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    @Override
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
