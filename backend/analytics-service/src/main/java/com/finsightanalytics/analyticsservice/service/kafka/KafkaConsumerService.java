package com.finsightanalytics.analyticsservice.kafka;

import com.finsightanalytics.analyticsservice.service.AnalyticsService;
import com.finsightanalytics.common.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @Autowired
    private AnalyticsService analyticsService;

    @KafkaListener(topics = "transaction-topic", groupId = "${spring.kafka.consumer-group}")
    public void consumeTransaction(Transaction transaction) {
        analyticsService.processTransaction(transaction);
    }
}
