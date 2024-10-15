package com.finsightanalytics.transactionservice.service;

import com.finsightanalytics.common.model.Transaction;
import com.finsightanalytics.transactionservice.repository.TransactionRepository;
import com.finsightanalytics.transactionservice.service.kafka.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    @Transactional
    public Transaction updateTransaction(Long id, Transaction transaction) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        transaction.setId(id); // Ensure the ID is set
        Transaction updatedTransaction = transactionRepository.save(transaction);
        kafkaProducerService.sendTransactionMessage(updatedTransaction); // Notify Kafka of the update
        return updatedTransaction;
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
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
