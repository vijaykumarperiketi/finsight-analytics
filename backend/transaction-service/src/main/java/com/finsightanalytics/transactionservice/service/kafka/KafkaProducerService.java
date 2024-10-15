package com.finsightanalytics.transactionservice.service.kafka;

import com.finsightanalytics.transactionservice.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TRANSACTION_TOPIC = "transaction-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendTransactionMessage(Transaction transaction) {
        try {
            String message = objectMapper.writeValueAsString(transaction);
            kafkaTemplate.send(TRANSACTION_TOPIC, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send transaction message", e);
        }
    }
}
