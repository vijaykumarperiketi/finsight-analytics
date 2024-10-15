package com.example.notificationservice.kafka;

import com.example.common.model.Notification;
import com.example.common.model.Transaction;
import com.example.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KafkaConsumerService {

    @Autowired
    private NotificationRepository notificationRepository;

    @KafkaListener(topics = "transaction-topic", groupId = "${spring.kafka.consumer-group}")
    public void consumeTransaction(Transaction transaction) {
        Notification notification = new Notification();
        notification.setUserId(transaction.getUserId());
        notification.setMessage("Transaction of " + transaction.getAmount() + " " + transaction.getType());
        notification.setDelivered(false);
        notification.setTimestamp(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}