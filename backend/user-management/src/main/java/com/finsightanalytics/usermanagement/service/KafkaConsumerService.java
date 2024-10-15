package com.finsightanalytics.usermanagement.service;

import com.finsightanalytics.common.model.Transaction;
import com.finsightanalytics.usermanagement.service.UserService;
import com.finsightanalytics.usermanagement.exception.InvalidTransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @Autowired
    private UserService userService;

    @KafkaListener(topics = "transaction-topic", groupId = "${spring.kafka.consumer-group}")
    public void consumeTransaction(Transaction transaction) {
        try {
            // Validate transaction type: debit or credit
            if (!"debit".equalsIgnoreCase(transaction.getType()) && !"credit".equalsIgnoreCase(transaction.getType())) {
                throw new InvalidTransactionException("Invalid transaction type: " + transaction.getType());
            }

            // Pass the transaction details to userService to update the financial goals or debts based on the transaction
            userService.updateFinancialGoalsAndDebtsBasedOnTransaction(
                    transaction.getUserId(),
                    transaction.getFinancialGoalId(),
                    transaction.getDebtId(),
                    transaction.getType(),
                    transaction.getAmount()
            );

        } catch (Exception e) {
            // Handle any exceptions that may occur during the processing
            System.err.println("Error processing transaction: " + e.getMessage());
        }
    }
}
