package com.finsightanalytics.recommendationengine.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import static org.neo4j.driver.Values.parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finsightanalytics.common.model.RecommendationType;
import com.finsightanalytics.recommendationengine.model.Neo4jRecommendation;
import com.finsightanalytics.recommendationengine.service.RecommendationService;

import jakarta.transaction.Transactional;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private Driver neo4jDriver;

    // Map of recommendation frequencies (in days)
    private static final Map<RecommendationType, Integer> RECOMMENDATION_FREQUENCIES = Map.of(
            RecommendationType.PEER_BASED, 7,
            RecommendationType.GOAL_BASED, 1,
            RecommendationType.DEBT_BASED, 1,
            RecommendationType.TRANSACTION_BASED, 1,
            RecommendationType.LIFECYCLE_BASED, 30
    );

    @Transactional
    @Override
    public List<Neo4jRecommendation> fetchRecentRecommendations(Long userId) {
        String query = """
                           MATCH (u:User {userId: $userId})<-[:ASSIGNED_TO]-(r:Recommendation)
                           RETURN r
                           ORDER BY r.dateCreated DESC
                           LIMIT 10
                       """;
        try (Session session = neo4jDriver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run(query, parameters("userId", userId));
                List<Neo4jRecommendation> recs = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    Neo4jRecommendation rec = new Neo4jRecommendation(record.get("r").asNode());
                    recs.add(rec);
                }
                return recs;
            });
        }
    }

    @Transactional
    @Override
    public List<Neo4jRecommendation> fetchAllRecommendations(Long userId) {
        String query = """
                           MATCH (u:User {userId: $userId})<-[:ASSIGNED_TO]-(r:Recommendation)
                           RETURN r
                           ORDER BY r.dateCreated DESC
                       """;
        try (Session session = neo4jDriver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run(query, parameters("userId", userId));
                List<Neo4jRecommendation> recs = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    Neo4jRecommendation rec = new Neo4jRecommendation(record.get("r").asNode());
                    recs.add(rec);
                }
                return recs;
            });
        }
    }

    @Transactional
    @Override
    public List<Neo4jRecommendation> fetchRecommendationsByType(Long userId, RecommendationType type) {
        String query = """
                MATCH (u:User {userId: $userId})<-[:ASSIGNED_TO]-(r:Recommendation {type: $type})
                RETURN r
                ORDER BY r.dateCreated DESC
                """;
        try (Session session = neo4jDriver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run(query, parameters("userId", userId, "type", type.name()));
                List<Neo4jRecommendation> recs = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    Neo4jRecommendation rec = new Neo4jRecommendation(record.get("r").asNode());
                    recs.add(rec);
                }
                return recs;
            });
        }
    }

    @Transactional
    @Override
    public Neo4jRecommendation createCustomRecommendation(Long bankerId, Long userId, Neo4jRecommendation recommendation) {
        String query = """
                MATCH (u:User {userId: $userId})
                CREATE (r:Recommendation {
                    recommendationId: $recommendationId,
                    type: $type,
                    message: $message,
                    dateCreated: date($dateCreated),
                    bankerId: $bankerId
                })
                CREATE (u)<-[:ASSIGNED_TO (r:Recommendation {type: "CUSTOM"})]-(r)
                RETURN r
                """;
        try (Session session = neo4jDriver.session()) {
            return session.writeTransaction(tx -> {
                long recommendationId = generateUniqueId();
                recommendation.setRecommendationId(recommendationId);
                recommendation.setType(RecommendationType.CUSTOM);
                recommendation.setDateCreated(LocalDateTime.now());
                Map<String, Object> params = Map.of(
                        "userId", userId,
                        "recommendationId", recommendationId,
                        "type", recommendation.getType().name(),
                        "message", recommendation.getMessage(),
                        "dateCreated", recommendation.getDateCreated().toString(),
                        "bankerId", bankerId
                );
                Result result = tx.run(query, params);
                if (result.hasNext()) {
                    Record record = result.next();
                    return new Neo4jRecommendation(record.get("r").asNode());
                } else {
                    throw new RuntimeException("Failed to create custom recommendation");
                }
            });
        }
    }

    @Override
    public void generateRecommendationsForUser(Long userId) {
        generatePeerBasedRecommendations(userId);
        generateGoalBasedRecommendations(userId);
        generateDebtBasedRecommendations(userId);
        generateTransactionBasedRecommendations(userId);
        generateLifecycleBasedRecommendations(userId);
    }

    private boolean shouldGenerateRecommendations(Long userId, RecommendationType type) {
        int frequency = RECOMMENDATION_FREQUENCIES.getOrDefault(type, 1);
        LocalDate now = LocalDate.now();

        try (Session session = neo4jDriver.session()) {
            return session.readTransaction(tx -> {
                String query = """
                        MATCH (u:User {userId: $userId})<-[:ASSIGNED_TO]-(r:Recommendation {type: $type})
                        RETURN r
                        ORDER BY r.dateCreated DESC
                        LIMIT 1
                        """;
                Result result = tx.run(query, parameters("userId", userId, "type", type.name()));
                if (result.hasNext()) {
                    Record record = result.next();
                    LocalDate lastGeneratedDate = record.get("r").get("dateCreated").asLocalDate();
                    
                    return lastGeneratedDate.plusDays(frequency).isBefore(now) || lastGeneratedDate.plusDays(frequency).isEqual(now);
                } else {
                    // No previous recommendation of this type exists, so generate one
                    return true;
                }
            });
        }
    }

    @Transactional
    @Override
    public void generatePeerBasedRecommendations(Long userId) {
        RecommendationType type = RecommendationType.PEER_BASED;
        if (!shouldGenerateRecommendations(userId, type)) {
            return;
        }
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                // Delete old recommendations of this type
                String deleteQuery = """
                        MATCH (u:User {userId: $userId})<-[:ASSIGNED_TO]-(r:Recommendation {type: $type})
                        DETACH DELETE r
                        """;
                tx.run(deleteQuery, parameters("userId", userId, "type", type.name()));

                // Fetch user's total savings
                String userQuery = """
                        MATCH (u:User {userId: $userId})
                        RETURN u.totalSavings AS userSavings
                        """;
                Result userResult = tx.run(userQuery, parameters("userId", userId));
                if (!userResult.hasNext()) {
                    return null;
                }
                double userSavings = userResult.next().get("userSavings").asDouble(0.0);

                // Calculate average peer savings
                String peerQuery = """
                        MATCH (u:User {userId: $userId})-[:BELONGS_TO]->(pg:PeerGroup)<-[:BELONGS_TO]-(peer:User)
                        WHERE peer.userId <> $userId
                        RETURN AVG(peer.totalSavings) AS avgPeerSavings
                        """;
                Result peerResult = tx.run(peerQuery, parameters("userId", userId));
                double avgPeerSavings = peerResult.hasNext() ? peerResult.next().get("avgPeerSavings").asDouble(0.0) : 0.0;

                // Check if user's savings are significantly below peers
                if (userSavings < avgPeerSavings * 0.8) {
                    // Create a new recommendation
                    String createRecQuery = """
                            MATCH (u:User {userId: $userId})
                            CREATE (r:Recommendation {
                                recommendationId: $recommendationId,
                                type: $type,
                                message: $message,
                                dateCreated: date($dateCreated)
                            })
                            CREATE (u)<-[:ASSIGNED_TO]-(r)
                            """;
                    Map<String, Object> params = Map.of(
                            "userId", userId,
                            "recommendationId", generateUniqueId(),
                            "type", type.name(),
                            "message", "Your savings are below your peers. Consider increasing your savings.",
                            "dateCreated", LocalDate.now().toString()
                    );
                    tx.run(createRecQuery, params);
                }
                return null;
            });
        }
    }

    @Override
    public void generateGoalBasedRecommendations(Long userId) {
        RecommendationType type = RecommendationType.GOAL_BASED;
        if (!shouldGenerateRecommendations(userId, type)) {
            return;
        }
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                // Delete old recommendations of this type for the user
                String deleteQuery = """
                    MATCH (u:User {userId: $userId})<-[:ASSIGNED_TO]-(r:Recommendation {type: $type})
                    DETACH DELETE r
                    """;
                tx.run(deleteQuery, parameters("userId", userId, "type", type.name()));

                // Fetch all active financial goals for the user
                String goalsQuery = """
                    MATCH (u:User {userId: $userId})<-[:GOAL_OF]-(g:FinancialGoal)
                    RETURN g
                    """;
                Result goalsResult = tx.run(goalsQuery, parameters("userId", userId));

                // Iterate over each financial goal
                while (goalsResult.hasNext()) {
                    Record record = goalsResult.next();

                    String goalName = record.get("name").asString();
                    double currentAmount = record.get("currentAmount").asDouble(0.0);
                    double targetAmount = record.get("targetAmount").asDouble(0.0);
                    LocalDate startDate = record.get("startDate").asLocalDate();
                    LocalDate endDate = record.get("endDate").asLocalDate();
                    

                    // Calculate health score
                    long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
                    long daysElapsed = ChronoUnit.DAYS.between(startDate, LocalDate.now());
                    if (daysElapsed < 1) daysElapsed = 1;

                    double expectedAmount = (daysElapsed / (double) totalDays) * targetAmount;
                    double healthScore = (currentAmount / expectedAmount) * 100;
                    
                    if (healthScore > 100) healthScore = 100;

                    // Determine health status
                    String healthStatus;
                    if (healthScore < 50) {
                        healthStatus = "unhealthy";
                    } else if (healthScore < 80) {
                        healthStatus = "moderate";
                    } else {
                        healthStatus = "healthy";
                    }

                    // Check if recommendation is needed
                    if (!"healthy".equals(healthStatus)) {
                        String message = "Your goal '" + goalName + "' is " + healthStatus +
                                ". Consider adjusting your contributions.";

                        // Create recommendation for this goal
                        String createRecQuery = """
                            MATCH (u:User {userId: $userId})
                            CREATE (r:Recommendation {
                                recommendationId: $recommendationId,
                                type: $type,
                                message: $message,
                                dateCreated: date($dateCreated)
                            })
                            CREATE (u)<-[:ASSIGNED_TO]-(r)
                            """;
                        Map<String, Object> params = Map.of(
                                "userId", userId,
                                "recommendationId", generateUniqueId(),
                                "type", type.name(),
                                "message", message,
                                "dateCreated", LocalDate.now().toString()
                        );
                        tx.run(createRecQuery, params);
                    }
                }
                return null;
            });
        }
    }

    @Override
    public void generateDebtBasedRecommendations(Long userId) {
        RecommendationType type = RecommendationType.DEBT_BASED;
        if (!shouldGenerateRecommendations(userId, type)) {
            return;
        }
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                String deleteQuery = """
                    MATCH (u:User {userId: $userId})<-[:ASSIGNED_TO]-(r:Recommendation {type: $type})
                    DETACH DELETE r
                    """;
                tx.run(deleteQuery, parameters("userId", userId, "type", type.name()));

                String debtQuery = """
                    MATCH (u:User {userId: $userId})<-[:DEBT_OF]-(d:Debt)
                    OPTIONAL MATCH (d)<-[:CONTRIBUTES_TO]-(t:Transaction)
                    WITH d, max(t.timeStamp) AS lastTransactionDate
                    RETURN d.debtId AS debtId, d.name AS name, d.paidAmount AS paidAmount, d.totalAmount AS totalAmount,
                           d.startDate AS startDate, d.endDate AS endDate, d.scheduledPayment AS scheduledPayment,
                           d.frequency AS frequency, d.lastPaymentDate AS lastPaymentDate, lastTransactionDate
                    """;
                Result debtResult = tx.run(debtQuery, parameters("userId", userId));
                
                while (debtResult.hasNext()) {
                    Record record = debtResult.next();
                    String debtName = record.get("name").asString();
                    double paidAmount = record.get("paidAmount").asDouble(0.0);
                    double totalAmount = record.get("totalAmount").asDouble(0.0);
                    LocalDate startDate = record.get("startDate").asLocalDate();
                    LocalDate endDate = record.get("endDate").asLocalDate();

                    // Calculate health score
                    long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
                    long daysElapsed = ChronoUnit.DAYS.between(startDate, LocalDate.now());
                    if (daysElapsed < 1) daysElapsed = 1;

                    double expectedPaidAmount = (daysElapsed / (double) totalDays) * totalAmount;
                    double healthScore = (paidAmount / expectedPaidAmount) * 100;
                    if (healthScore > 100) healthScore = 100;

                    // Determine health status
                    String healthStatus;
                    if (healthScore < 50) {
                        healthStatus = "unhealthy";
                    } else if (healthScore < 80) {
                        healthStatus = "moderate";
                    } else {
                        healthStatus = "healthy";
                    }

                    // Check if recommendation is needed
                    if (!"healthy".equals(healthStatus)) {
                        String message = "Your debt '" + debtName + "' is " + healthStatus + "(" + healthScore + ")" +
                                ". Consider adjusting your payments to stay on track.";

                        String createRecQuery = """
                            MATCH (u:User {userId: $userId})
                            CREATE (r:Recommendation {
                                recommendationId: $recommendationId,
                                type: $type,
                                message: $message,
                                dateCreated: date($dateCreated)
                            })
                            CREATE (u)<-[:ASSIGNED_TO]-(r)
                            """;
                        Map<String, Object> params = Map.of(
                                "userId", userId,
                                "recommendationId", generateUniqueId(),
                                "type", type.name(),
                                "message", message,
                                "dateCreated", LocalDate.now().toString()
                        );
                        tx.run(createRecQuery, params);
                    }
                }
                return null;
            });
        }
    }

    @Override
    public void generateTransactionBasedRecommendations(Long userId) {
        RecommendationType type = RecommendationType.TRANSACTION_BASED;
        if (!shouldGenerateRecommendations(userId, type)) {
            return;
        }
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                String deleteQuery = """
                        MATCH (u:User {userId: $userId})<-[:ASSIGNED_TO]-(r:Recommendation {type: $type})
                        DETACH DELETE r
                        """;
                tx.run(deleteQuery, parameters("userId", userId, "type", type.name()));

                String transactionsQuery = """
                        MATCH (u:User {userId: $userId})<-[:MADE_BY]-(t:Transaction)
                        WHERE t.timeStamp >= datetime($oneMonthAgo)
                        RETURN t.type AS type, SUM(t.amount) AS totalAmount
                        """;
                Result result = tx.run(transactionsQuery, parameters("userId", userId, "oneMonthAgo", oneMonthAgo.toString()));

                double totalCredits = 0.0;
                double totalDebits = 0.0;

                while (result.hasNext()) {
                    Record record = result.next();
                    String transactionType = record.get("type").asString();
                    double amount = record.get("totalAmount").asDouble(0.0);
                    if ("CREDIT".equalsIgnoreCase(transactionType)) {
                        totalCredits += amount;
                    } else if ("DEBIT".equalsIgnoreCase(transactionType)) {
                        totalDebits += amount;
                    }
                }

                if (totalDebits > totalCredits) {
                    String createRecQuery = """
                            MATCH (u:User {userId: $userId})
                            CREATE (r:Recommendation {
                                recommendationId: $recommendationId,
                                type: $type,
                                message: $message,
                                dateCreated: date($dateCreated)
                            })
                            CREATE (u)<-[:ASSIGNED_TO]-(r)
                            """;
                    Map<String, Object> params = Map.of(
                            "userId", userId,
                            "recommendationId", generateUniqueId(),
                            "type", type.name(),
                            "message", "Your spending has exceeded your income in the past month. Consider reviewing your expenses.",
                            "dateCreated", LocalDate.now().toString()
                    );
                    tx.run(createRecQuery, params);
                }
                return null;
            });
        }
    }

    @Override
    public void generateLifecycleBasedRecommendations(Long userId) {
        RecommendationType type = RecommendationType.LIFECYCLE_BASED;
        if (!shouldGenerateRecommendations(userId, type)) {
            return;
        }
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                String deleteQuery = """
                        MATCH (u:User {userId: $userId})<-[:ASSIGNED_TO]-(r:Recommendation {type: $type})
                        DETACH DELETE r
                        """;
                tx.run(deleteQuery, parameters("userId", userId, "type", type.name()));

                // Fetch user's financial goals
                String goalsQuery = """
                        MATCH (u:User {userId: $userId})<-[:GOAL_OF|ACHIEVED_BY]-(g:FinancialGoal)
                        RETURN g.name AS goalName
                        """;
                Result goalsResult = tx.run(goalsQuery, parameters("userId", userId));
                List<String> goalNames = new ArrayList<>();
                while (goalsResult.hasNext()) {
                    Record record = goalsResult.next();
                    goalNames.add(record.get("goalName").asString());
                }

                // Determine lifecycle stage based on goals
                String lifecycleStage = inferLifecycleStage(goalNames);

                if (lifecycleStage != null) {
                    String message = getLifecycleRecommendation(lifecycleStage);
                    String createRecQuery = """
                            MATCH (u:User {userId: $userId})
                            CREATE (r:Recommendation {
                                recommendationId: $recommendationId,
                                type: $type,
                                message: $message,
                                dateCreated: date($dateCreated)
                            })
                            CREATE (u)<-[:ASSIGNED_TO]-(r)
                            """;
                    Map<String, Object> params = Map.of(
                            "userId", userId,
                            "recommendationId", generateUniqueId(),
                            "type", type.name(),
                            "message", message,
                            "dateCreated", LocalDate.now().toString()
                    );
                    tx.run(createRecQuery, params);
                }
                return null;
            });
        }
    }

    private String inferLifecycleStage(List<String> goalNames) {
        if (goalNames == null || goalNames.isEmpty()) {
            return null;
        }
        Map<String, String> keywordToStage = Map.ofEntries(
            Map.entry("Emergency Fund", "Early Career"),
            Map.entry("First Car", "Early Career"),
            Map.entry("Travel", "Exploration"),
            Map.entry("Education", "Exploration"),
            Map.entry("Start a Business", "Entrepreneurship"),
            Map.entry("Investments", "Wealth Building"),
            Map.entry("Wedding", "Family Planning"),
            Map.entry("First Home", "Family Planning"),
            Map.entry("Children", "Family Planning"),
            Map.entry("Health and Wellness", "Self Development"),
            Map.entry("Tech Gadgets", "Lifestyle"),
            Map.entry("Social Causes", "Philanthropy"),
            Map.entry("Retirement", "Long-Term Planning")
        );

        Map<String, Integer> stageCount = new HashMap<>();
        for (String goalName : goalNames) {
            for (Map.Entry<String, String> entry : keywordToStage.entrySet()) {
                if (goalName != null && goalName.toLowerCase().contains(entry.getKey().toLowerCase())) {
                    stageCount.merge(entry.getValue(), 1, Integer::sum);
                }
            }
        }
        return stageCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("General");
    }

    private String getLifecycleRecommendation(String lifecycleStage) {
        return switch (lifecycleStage) {
            case "Early Career" -> "As you're starting your career, focus on building an emergency fund and managing expenses.";
            case "Exploration" -> "Consider budgeting for travel and further education to enrich your experiences.";
            case "Entrepreneurship" -> "Explore funding options and build a solid business plan for your startup ambitions.";
            case "Wealth Building" -> "Diversify your investments and consider long-term financial growth strategies.";
            case "Family Planning" -> "Plan for major life events like weddings and buying a home by setting clear savings goals.";
            case "Self Development" -> "Invest in your health and personal growth activities.";
            case "Lifestyle" -> "Budget for lifestyle upgrades while ensuring you maintain healthy savings.";
            case "Philanthropy" -> "Incorporate charitable giving into your financial plan in a sustainable way.";
            case "Long-Term Planning" -> "Start considering long-term goals like retirement savings and estate planning.";
            default -> "Review your financial goals and consider diversifying your investments.";
        };
    }

    private long generateUniqueId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }
}   
