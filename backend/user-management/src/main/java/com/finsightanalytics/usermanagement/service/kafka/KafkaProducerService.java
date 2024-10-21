package com.finsightanalytics.usermanagement.kafka;

import com.finsightanalytics.common.model.User;
import com.finsightanalytics.common.model.Goal;
import com.finsightanalytics.common.model.Debt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String USER_CREATED_TOPIC = "user-created";
    private static final String USER_UPDATED_TOPIC = "user-updated";
    private static final String USER_DELETED_TOPIC = "user-deleted";
    private static final String GOAL_CREATED_TOPIC = "goal-created";
    private static final String GOAL_UPDATED_TOPIC = "goal-updated";
    private static final String GOAL_FINISHED_TOPIC = "goal-finished";
    private static final String DEBT_CREATED_TOPIC = "debt-created";
    private static final String DEBT_UPDATED_TOPIC = "debt-updated";
    private static final String DEBT_CLEARED_TOPIC = "debt-cleared";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Produce user created event
    public void sendUserCreatedEvent(User user) {
        sendEvent(USER_CREATED_TOPIC, user);
    }

    // Produce user updated event
    public void sendUserUpdatedEvent(User user) {
        sendEvent(USER_UPDATED_TOPIC, user);
    }

    // Produce user deleted event
    public void sendUserDeletedEvent(User user) {
        sendEvent(USER_DELETED_TOPIC, user);
    }

    // Produce goal created event
    public void sendGoalCreatedEvent(Goal goal) {
        sendEvent(GOAL_CREATED_TOPIC, goal);
    }

    // Produce goal updated event
    public void sendGoalUpdatedEvent(Goal goal) {
        sendEvent(GOAL_UPDATED_TOPIC, goal);
    }

    // Produce goal finished event
    public void sendGoalFinishedEvent(Goal goal) {
        sendEvent(GOAL_FINISHED_TOPIC, goal);
    }

    // Produce debt created event
    public void sendDebtCreatedEvent(Debt debt) {
        sendEvent(DEBT_CREATED_TOPIC, debt);
    }

    // Produce debt updated event
    public void sendDebtUpdatedEvent(Debt debt) {
        sendEvent(DEBT_UPDATED_TOPIC, debt);
    }

    // Produce debt cleared event
    public void sendDebtClearedEvent(Debt debt) {
        sendEvent(DEBT_CLEARED_TOPIC, debt);
    }

    // Helper method to send any event
    private void sendEvent(String topic, Object payload) {
        try {
            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }
}
