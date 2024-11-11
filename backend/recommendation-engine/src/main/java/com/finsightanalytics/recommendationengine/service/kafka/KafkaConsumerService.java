package com.finsightanalytics.recommendationengine.service.kafka;

import java.math.BigDecimal;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.exceptions.Neo4jException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.finsightanalytics.common.exception.InvalidTransactionException;
import com.finsightanalytics.common.model.Debt;
import com.finsightanalytics.common.model.FinancialGoal;
import com.finsightanalytics.common.model.Transaction;
import com.finsightanalytics.common.model.TransactionType;
import com.finsightanalytics.common.model.User;

import jakarta.transaction.Transactional;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private Driver neo4jDriver;

    @KafkaListener(topics = "user-created", groupId = "recommendation-engine-group")
    @Transactional
    public void handleUserCreatedEvent(User user) {
        logger.info("Received user-created event: {}", user);
        
        String query = """
                MERGE (pg:PeerGroup {ifscCode: $ifscCode})
                CREATE (u:User {
                    userId: $userId,
                    name: $name,
                    email: $email,
                    phoneNumber: $phoneNumber,
                    role: $role,
                    totalSavings: $totalSavings,
                    balance: $balance
                })
                CREATE (u)-[:BELONGS_TO]->(pg)
                """;
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run(query, parameters(
                        "ifscCode", user.getIfscCode(),
                        "userId", user.getUserId(),
                        "name", user.getUsername(),
                        "email", user.getEmail(),
                        "phoneNumber", user.getPhoneNumber(),
                        "role", user.getRole(),
                        "totalSavings", 0.0,
                        "balance", 0.0
                ));
                return null;
            });
            logger.info("User node created successfully for userId: {}", user.getUserId());
        } catch (Neo4jException e) {
            logger.error("Error creating user node for userId: {}. Error: {}", user.getUserId(), e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = "transaction-created", groupId = "recommendation-engine-group")
    @Transactional
    public void handleTransactionCreatedEvent(Transaction transaction) {
        logger.info("Received transaction-created event: {}", transaction);
        
        String createTransactionQuery = """
                MATCH (u:User {userId: $userId})
                CREATE (t:Transaction {
                    transactionId: $transactionId,
                    amount: $amount,
                    type: $type,
                    timeStamp: datetime($timeStamp)
                })
                CREATE (u)<-[:MADE_BY]-(t)
                """;

        try (Session session = neo4jDriver.session()) {
            logger.debug("Creating transaction node for transactionId: {}", transaction.getTransactionId());
            
            session.writeTransaction(tx -> {
                tx.run(createTransactionQuery, parameters(
                        "userId", transaction.getUserId(),
                        "transactionId", transaction.getTransactionId(),
                        "amount", transaction.getAmount(),
                        "type", transaction.getType(),
                        "timeStamp", transaction.getTimestamp().toString()
                ));

                TransactionType transactionType = transaction.getType();
                Long userId = transaction.getUserId();
                BigDecimal amount = transaction.getAmount();

                switch (transactionType) {
                    case CREDIT -> {
                        if (transaction.getFinancialGoalId() != null) {
                            logger.debug("Processing credit transaction related to financial goalId: {}", transaction.getFinancialGoalId());
                            // Credit transaction with financialGoalId updates total savings
                            String goalRelQuery = """
                                                      MATCH (g:FinancialGoal {financialGoalId: $financialGoalId})
                                                      MATCH (t:Transaction {transactionId: $transactionId})
                                                      CREATE (t)-[:CONTRIBUTES_TO]->(g)
                                                      SET g.currentAmount = coalesce(g.currentAmount, 0) + $amount
                                                  """;
                            tx.run(goalRelQuery, parameters(
                                    "financialGoalId", transaction.getFinancialGoalId(),
                                    "transactionId", transaction.getTransactionId(),
                                    "amount", amount
                            ));
                            
                            String updateSavingsQuery = """
                                                            MATCH (u:User {userId: $userId})
                                                            SET u.totalSavings = coalesce(u.totalSavings, 0) + $amount
                                                        """;
                            tx.run(updateSavingsQuery, parameters(
                                    "userId", userId,
                                    "amount", amount
                            ));
                            
                            // Check for goal achievement
                            String checkGoalQuery = """
                                                        MATCH (g:FinancialGoal {financialGoalId: $financialGoalId})
                                                        WHERE g.currentAmount >= g.targetAmount
                                                        MATCH (u:User {userId: $userId})<-[rel:GOAL_OF]-(g)
                                                        DELETE rel
                                                        CREATE (u)<-[:ACHIEVED_BY {achievedDate: date()}]-(g)
                                                    """;
                            tx.run(checkGoalQuery, parameters(
                                    "financialGoalId", transaction.getFinancialGoalId(),
                                    "userId", userId
                            ));
                            logger.info("Processed credit transaction for financial goalId: {}", transaction.getFinancialGoalId());
                        } else if (transaction.getDebtId() != null) {
                            logger.debug("Processing credit transaction related to debtId: {}", transaction.getDebtId());
                            // Credit transaction with debtId updates neither total savings nor balance
                            String debtRelQuery = """
                                                      MATCH (d:Debt {debtId: $debtId})
                                                      MATCH (t:Transaction {transactionId: $transactionId})
                                                      CREATE (t)-[:CONTRIBUTES_TO]-(d)
                                                      SET d.paidAmount = coalesce(d.paidAmount, 0) + $amount
                                                  """;
                            tx.run(debtRelQuery, parameters(
                                    "debtId", transaction.getDebtId(),
                                    "transactionId", transaction.getTransactionId(),
                                    "amount", amount
                            ));
                            
                            // Check for debt clearance
                            String checkDebtQuery = """
                                                        MATCH (d:Debt {debtId: $debtId})
                                                        WHERE d.paidAmount >= d.totalAmount
                                                        MATCH (u:User {userId: $userId})<-[rel:DEBT_OF]-(d)
                                                        DELETE rel
                                                        CREATE (u)<-[:CLEARED_BY {clearedDate: date()}]-(d)
                                                    """;
                            tx.run(checkDebtQuery, parameters(
                                    "debtId", transaction.getDebtId(),
                                    "userId", userId
                            ));
                            
                            // No update to total savings or balance
                            logger.info("Processed credit transaction for debtId: {}", transaction.getDebtId());
                        } else {
                            // Credit transaction without financialGoalId updates user balance
                            logger.debug("Processing credit transaction updating user balance.");
                            String updateBalanceQuery = """
                                                            MATCH (u:User {userId: $userId})
                                                            SET u.balance = coalesce(u.balance, 0) + $amount
                                                            """;
                            tx.run(updateBalanceQuery, parameters(
                                    "userId", userId,
                                    "amount", amount
                            ));
                            logger.info("User balance updated for userId: {}", userId);
                        }
                    }
                    case DEBIT -> {
                        if (transaction.getFinancialGoalId() != null || transaction.getDebtId() != null) {
                            // Debit transaction cannot have financialGoalId or debtId
                            throw new InvalidTransactionException("Debit transactions cannot have financialGoalId or debtId.");
                        } else {
                            // Debit transaction updates user balance
                            logger.debug("Processing debit transaction updating user balance.");

                            String updateBalanceQuery = """
                                                            MATCH (u:User {userId: $userId})
                                                            SET u.balance = coalesce(u.balance, 0) - $amount
                                                        """;
                            tx.run(updateBalanceQuery, parameters(
                                    "userId", userId,
                                    "amount", amount
                            ));
                            
                            logger.info("User balance updated for userId: {}", userId);
                        }
                    }
                    default -> throw new InvalidTransactionException("Invalid transaction type: " + transaction.getType());
                }

                return null;
            });
        }
        catch (Neo4jException | InvalidTransactionException e) {
            logger.error("Error processing transaction-created event for transactionId: {}. Error: {}", transaction.getTransactionId(), e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = "goal-created", groupId = "recommendation-engine-group")
    @Transactional
    public void handleGoalCreatedEvent(FinancialGoal financialGoal) {
        logger.info("Received goal-created event: {}", financialGoal);

        String query = """
                MATCH (u:User {userId: $userId})
                CREATE (g:FinancialGoal {
                    financialGoalId: $financialGoalId,
                    name: $name,
                    targetAmount: $targetAmount,
                    currentAmount: 0.0,
                    startDate: date($startDate),
                    endDate: date($endDate)
                })
                CREATE (u)<-[:GOAL_OF]-(g)
                """;
        try (Session session = neo4jDriver.session()) {
            logger.debug("Creating financial goal node for financialGoalId: {}", financialGoal.getFinancialGoalId());

            session.writeTransaction(tx -> {
                tx.run(query, parameters(
                        "userId", financialGoal.getUserId(),
                        "financialGoalId", financialGoal.getFinancialGoalId(),
                        "name", financialGoal.getName(),
                        "targetAmount", financialGoal.getTargetAmount(),
                        "startDate", financialGoal.getStartDate().toString(),
                        "endDate", financialGoal.getEndDate().toString()
                ));
                
                logger.info("Financial goal node created for financialGoalId: {}", financialGoal.getFinancialGoalId());
                return null;
            });
        }
        catch (Neo4jException e) {
            logger.error("Error creating financial goal for financialGoalId: {}. Error: {}", financialGoal.getFinancialGoalId(), e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = "debt-created", groupId = "recommendation-engine-group")
    @Transactional
    public void handleDebtCreatedEvent(Debt debt) {
        logger.info("Received debt-created event: {}", debt);

        String query = """
                MATCH (u:User {userId: $userId})
                CREATE (d:Debt {
                    debtId: $debtId,
                    name: $name,
                    totalAmount: $totalAmount,
                    paidAmount: 0.0,
                    startDate: date($startDate),
                    endDate: date($endDate)
                })
                CREATE (u)<-[:DEBT_OF]-(d)
                """;
        try (Session session = neo4jDriver.session()) {
            logger.debug("Creating debt node for debtId: {}", debt.getDebtId());

            session.writeTransaction(tx -> {
                tx.run(query, parameters(
                        "userId", debt.getUserId(),
                        "debtId", debt.getDebtId(),
                        "name", debt.getName(),
                        "totalAmount", debt.getTotalAmount(),
                        "startDate", debt.getStartDate().toString(),
                        "endDate", debt.getEndDate().toString()
                ));
                
                logger.info("Debt node created for debtId: {}", debt.getDebtId());
                return null;
            });
        }
        catch (Neo4jException e) {
            logger.error("Error creating debt for debtId: {}. Error: {}", debt.getDebtId(), e.getMessage());
            throw e;
        }
    }
}
