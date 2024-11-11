package com.finsightanalytics.usermanagement.service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.finsightanalytics.common.exception.InvalidTransactionException;
import com.finsightanalytics.common.model.Transaction;
import com.finsightanalytics.common.model.TransactionType;
import com.finsightanalytics.usermanagement.service.UserService;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private UserService userService;

    @KafkaListener(topics = "transaction-topic", groupId = "${spring.kafka.consumer-group}")
    public void consumeTransaction(Transaction transaction) {
        try {
            // Validate transaction type: debit or credit
            if (transaction.getType() != TransactionType.DEBIT && transaction.getType() != TransactionType.CREDIT) {
                throw new InvalidTransactionException("Invalid transaction type: " + transaction.getType().toString());
            }

            // Pass the transaction details to userService to update the financial goals or debts based on the transaction
            userService.updateFinancialGoalsAndDebtsBasedOnTransaction(
                    transaction.getUserId(),
                    transaction.getFinancialGoalId(),
                    transaction.getDebtId(),
                    transaction.getType().toString(),
                    transaction.getAmount()
            );

        } catch (KafkaException e) {
            // Handle any exceptions that may occur during the processing
            logger.error("Error processing transaction: ", e.getMessage());
        }
    }
}
