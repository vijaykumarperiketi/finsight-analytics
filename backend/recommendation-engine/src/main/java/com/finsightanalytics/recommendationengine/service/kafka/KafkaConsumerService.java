package com.finsightanalytics.recommendationengine.service.kafka;

import com.finsightanalytics.common.model.User;
import com.finsightanalytics.common.model.Transaction;
import com.finsightanalytics.common.model.Goal;
import com.finsightanalytics.common.model.Debt;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @Autowired
    private Driver neo4jDriver;

    // Handle User Created Event
    @KafkaListener(topics = "user-created", groupId = "${spring.kafka.consumer-group}")
    public void consumeUserCreated(User user) {
        try (Session session = neo4jDriver.session()) {
            String query = "CREATE (u:User {userId: $userId, name: $name, email: $email, balance: $balance})";
            session.run(query, 
                Map.of("userId", user.getId(), "name", user.getUsername(), "email", user.getEmail(), "balance", user.getBalance()));
        }
    }

    // Handle User Updated Event
    @KafkaListener(topics = "user-updated", groupId = "${spring.kafka.consumer-group}")
    public void consumeUserUpdated(User user) {
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId}) " +
                           "SET u.name = $name, u.email = $email, u.balance = $balance";
            session.run(query, 
                Map.of("userId", user.getId(), "name", user.getUsername(), "email", user.getEmail(), "balance", user.getBalance()));
        }
    }

    // Handle User Deleted Event
    @KafkaListener(topics = "user-deleted", groupId = "${spring.kafka.consumer-group}")
    public void consumeUserDeleted(User user) {
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId}) DETACH DELETE u";
            session.run(query, Map.of("userId", user.getId()));
        }
    }

    // Handle Transaction Created Event
    @KafkaListener(topics = "transaction-created", groupId = "${spring.kafka.consumer-group}")
    public void consumeTransactionCreated(Transaction transaction) {
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId}) " +
                           "CREATE (u)-[:MADE]->(t:Transaction {transactionId: $transactionId, amount: $amount, type: $type, timestamp: $timestamp})";
            session.run(query, 
                Map.of("userId", transaction.getUserId(), "transactionId", transaction.getId(), 
                       "amount", transaction.getAmount(), "type", transaction.getType(), "timestamp", transaction.getTimestamp()));
        }
    }

    // Handle Goal Created Event
    @KafkaListener(topics = "goal-created", groupId = "${spring.kafka.consumer-group}")
    public void consumeGoalCreated(Goal goal) {
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId}) " +
                           "CREATE (u)-[:HAS_GOAL]->(g:Goal {goalId: $goalId, name: $name, targetAmount: $targetAmount, currentAmount: $currentAmount})";
            session.run(query, 
                Map.of("userId", goal.getUserId(), "goalId", goal.getId(), 
                       "name", goal.getName(), "targetAmount", goal.getTargetAmount(), "currentAmount", goal.getCurrentAmount()));
        }
    }

    // Handle Goal Updated Event
    @KafkaListener(topics = "goal-updated", groupId = "${spring.kafka.consumer-group}")
    public void consumeGoalUpdated(Goal goal) {
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId}) " +
                           "SET g.name = $name, g.targetAmount = $targetAmount, g.currentAmount = $currentAmount";
            session.run(query, 
                Map.of("userId", goal.getUserId(), "goalId", goal.getId(), 
                       "name", goal.getName(), "targetAmount", goal.getTargetAmount(), "currentAmount", goal.getCurrentAmount()));
        }
    }

    // Handle Goal Finished Event
    @KafkaListener(topics = "goal-finished", groupId = "${spring.kafka.consumer-group}")
    public void consumeGoalFinished(Goal goal) {
        try (Session session = neo4jDriver.session()) {
            // Delete the existing HAS_GOAL relationship
            String deleteRelationshipQuery = "MATCH (u:User {userId: $userId})-[r:HAS_GOAL]->(g:Goal {goalId: $goalId}) DELETE r";
            session.run(deleteRelationshipQuery, 
                Map.of("userId", goal.getUserId(), "goalId", goal.getId()));

            // Create an ACHIEVED relationship from User to Goal
            String createAchievedRelationshipQuery = "MATCH (u:User {userId: $userId}) " +
                        "CREATE (u)-[:ACHIEVED]->(g:Goal {goalId: $goalId, name: $name, targetAmount: $targetAmount, currentAmount: $currentAmount})";
            session.run(createAchievedRelationshipQuery, 
                Map.of("userId", goal.getUserId(), "goalId", goal.getId(), 
                    "name", goal.getName(), "targetAmount", goal.getTargetAmount(), "currentAmount", goal.getCurrentAmount()));
        }
    }


    // Handle Debt Created Event
    @KafkaListener(topics = "debt-created", groupId = "${spring.kafka.consumer-group}")
    public void consumeDebtCreated(Debt debt) {
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId}) " +
                           "CREATE (u)-[:HAS_DEBT]->(d:Debt {debtId: $debtId, name: $name, totalAmount: $totalAmount, paidAmount: $paidAmount, dueDate: $dueDate})";
            session.run(query, 
                Map.of("userId", debt.getUserId(), "debtId", debt.getId(), 
                       "name", debt.getName(), "totalAmount", debt.getTotalAmount(), "paidAmount", debt.getPaidAmount(), "dueDate", debt.getDueDate()));
        }
    }

    // Handle Debt Updated Event
    @KafkaListener(topics = "debt-updated", groupId = "${spring.kafka.consumer-group}")
    public void consumeDebtUpdated(Debt debt) {
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId}) " +
                           "SET d.name = $name, d.totalAmount = $totalAmount, d.paidAmount = $paidAmount, d.dueDate = $dueDate";
            session.run(query, 
                Map.of("userId", debt.getUserId(), "debtId", debt.getId(), 
                       "name", debt.getName(), "totalAmount", debt.getTotalAmount(), "paidAmount", debt.getPaidAmount(), "dueDate", debt.getDueDate()));
        }
    }

    // Handle Debt Cleared Event
    @KafkaListener(topics = "debt-cleared", groupId = "${spring.kafka.consumer-group}")
    public void consumeDebtCleared(Debt debt) {
        try (Session session = neo4jDriver.session()) {
            // Delete the existing HAS_DEBT relationship
            String deleteRelationshipQuery = "MATCH (u:User {userId: $userId})-[r:HAS_DEBT]->(d:Debt {debtId: $debtId}) DELETE r";
            session.run(deleteRelationshipQuery, 
                Map.of("userId", debt.getUserId(), "debtId", debt.getId()));

            // Create a CLEARED relationship from User to Debt
            String createClearedRelationshipQuery = "MATCH (u:User {userId: $userId}) " +
                        "CREATE (u)-[:CLEARED]->(d:Debt {debtId: $debtId, name: $name, totalAmount: $totalAmount, paidAmount: $paidAmount, dueDate: $dueDate})";
            session.run(createClearedRelationshipQuery, 
                Map.of("userId", debt.getUserId(), "debtId", debt.getId(), 
                    "name", debt.getName(), "totalAmount", debt.getTotalAmount(), "paidAmount", debt.getPaidAmount(), "dueDate", debt.getDueDate()));
        }
    }
}
