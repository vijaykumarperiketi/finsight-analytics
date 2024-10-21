package com.finsightanalytics.recommendationengine.service;

import com.finsightanalytics.common.model.*;
import com.finsightanalytics.recommendationengine.repository.*;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import static org.neo4j.driver.Values.parameters;

@Service
public class RecommendationService {

    @Autowired
    private Driver neo4jDriver;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private DebtRepository debtRepository;

    // 1. Transaction-Based Recommendations
    public List<Recommendation> generateTransactionBasedRecommendations(Long userId) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Fetch user data
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            // Handle user not found
            return recommendations;
        }
        User user = userOpt.get();

        // Define the category to analyze
        String category = "Dining"; // Example category

        // Query Neo4j for transaction data
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId})-[:MADE]->(t:Transaction) " +
                           "WHERE t.category = $category AND t.date >= date() - duration('P2M') " +
                           "WITH u, t " +
                           "RETURN " +
                           "SUM(CASE WHEN t.date >= date() - duration('P1M') THEN t.amount ELSE 0 END) AS currentMonthSpending, " +
                           "SUM(CASE WHEN t.date < date() - duration('P1M') THEN t.amount ELSE 0 END) AS previousMonthSpending";
            Result result = session.run(query, parameters("userId", userId, "category", category));

            if (result.hasNext()) {
                Record record = result.next();
                double currentSpending = record.get("currentMonthSpending").asDouble();
                double previousSpending = record.get("previousMonthSpending").asDouble();

                if (previousSpending == 0) {
                    // No previous spending to compare
                    return recommendations;
                }

                double percentageIncrease = ((currentSpending - previousSpending) / previousSpending) * 100;

                if (percentageIncrease > 20) {
                    // Generate recommendation
                    Recommendation recommendation = new Recommendation();
                    recommendation.setUserId(userId);
                    recommendation.setType("transaction-based");
                    recommendation.setMessage(String.format(
                        "You have spent %.2f%% more on %s this month compared to last month. Consider adjusting your budget.",
                        percentageIncrease, category));
                    recommendation.setDateCreated(LocalDate.now());
                    recommendations.add(recommendation);

                    // Save recommendation
                    recommendationRepository.save(recommendation);
                }
            }
        }
        return recommendations;
    }

    // 2. Financial Goal Recommendations
    public List<Recommendation> generateFinancialGoalRecommendations(Long userId, Long goalId) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Fetch user and goal data
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Goal> goalOpt = goalRepository.findById(goalId);

        if (userOpt.isEmpty() || goalOpt.isEmpty()) {
            // Handle user or goal not found
            return recommendations;
        }
        User user = userOpt.get();
        Goal goal = goalOpt.get();

        // Calculate expected progress
        LocalDate now = LocalDate.now();
        long totalMonths = ChronoUnit.MONTHS.between(goal.getStartDate(), goal.getTargetDate());
        long monthsElapsed = ChronoUnit.MONTHS.between(goal.getStartDate(), now);

        if (totalMonths <= 0 || monthsElapsed < 0) {
            // Invalid goal dates
            return recommendations;
        }

        double expectedProgress = (monthsElapsed / (double) totalMonths) * goal.getGoalAmount();

        if (goal.getProgress() < expectedProgress) {
            double additionalAmount = (expectedProgress - goal.getProgress()) / (totalMonths - monthsElapsed);

            Recommendation recommendation = new Recommendation();
            recommendation.setUserId(userId);
            recommendation.setType("financial-goal");
            recommendation.setMessage(String.format(
                "You are behind on your %s goal. Increasing your monthly contribution by $%.2f could help you reach your target on time.",
                goal.getGoalName(), additionalAmount));
            recommendation.setDateCreated(LocalDate.now());
            recommendations.add(recommendation);

            // Save recommendation
            recommendationRepository.save(recommendation);
        }
        return recommendations;
    }

    // 3. Debt Management Recommendations
    public List<Recommendation> generateDebtManagementRecommendations(Long userId, Long debtId) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Fetch user and debt data
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Debt> debtOpt = debtRepository.findById(debtId);

        if (userOpt.isEmpty() || debtOpt.isEmpty()) {
            // Handle user or debt not found
            return recommendations;
        }
        Debt debt = debtOpt.get();

        // Check if the user is behind on debt repayment
        if (debt.getNextPaymentDate().isBefore(LocalDate.now())) {
            Recommendation recommendation = new Recommendation();
            recommendation.setUserId(userId);
            recommendation.setType("debt-goal");
            recommendation.setMessage(String.format(
                "You are behind on your %s payments. Please consider making a payment to avoid additional interest or penalties.",
                debt.getDebtType()));
            recommendation.setDateCreated(LocalDate.now());
            recommendations.add(recommendation);

            // Save recommendation
            recommendationRepository.save(recommendation);
        }
        return recommendations;
    }

    // 4. Peer-Based Recommendations
    public List<Recommendation> generatePeerBasedRecommendations(Long userId) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Fetch user data
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            // Handle user not found
            return recommendations;
        }

        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId})-[:BELONGS_TO]->(pg:PeerGroup)<-[:BELONGS_TO]-(peer:User) " +
                           "WHERE peer.userId <> u.userId " +
                           "RETURN u.savingsRate AS userSavingsRate, AVG(peer.savingsRate) AS peerAvgSavingsRate";
            Result result = session.run(query, parameters("userId", userId));

            if (result.hasNext()) {
                Record record = result.next();
                double userSavingsRate = record.get("userSavingsRate").asDouble();
                double peerAvgSavingsRate = record.get("peerAvgSavingsRate").asDouble();

                if (userSavingsRate < peerAvgSavingsRate - 0.05) { // 5% less than peers
                    Recommendation recommendation = new Recommendation();
                    recommendation.setUserId(userId);
                    recommendation.setType("peer-based");
                    recommendation.setMessage(String.format(
                        "Your savings rate is %.2f%%, which is below the peer average of %.2f%%. Consider increasing your monthly savings.",
                        userSavingsRate * 100, peerAvgSavingsRate * 100));
                    recommendation.setDateCreated(LocalDate.now());
                    recommendations.add(recommendation);

                    // Save recommendation
                    recommendationRepository.save(recommendation);
                }
            }
        }
        return recommendations;
    }

    // 5. Lifecycle-Based Recommendations
    public List<Recommendation> generateLifecycleBasedRecommendations(Long userId) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Fetch user data
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            // Handle user not found
            return recommendations;
        }

        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User {userId: $userId})-[:IN_LIFECYCLE]->(l:Lifecycle) " +
                           "RETURN l.name AS lifecycleName";
            Result result = session.run(query, parameters("userId", userId));

            if (result.hasNext()) {
                Record record = result.next();
                String lifecycleName = record.get("lifecycleName").asString();

                Recommendation recommendation = new Recommendation();
                recommendation.setUserId(userId);
                recommendation.setType("lifecycle-based");
                recommendation.setMessage(String.format(
                    "Based on your current lifecycle stage (%s), consider focusing on %s.",
                    lifecycleName, getLifecycleRecommendation(lifecycleName)));
                recommendation.setDateCreated(LocalDate.now());
                recommendations.add(recommendation);

                // Save recommendation
                recommendationRepository.save(recommendation);
            }
        }
        return recommendations;
    }

    // Helper method to get lifecycle-specific advice
    private String getLifecycleRecommendation(String lifecycleName) {
        switch (lifecycleName) {
            case "Early Career":
                return "building an emergency fund and starting to save for retirement";
            case "Mid Career":
                return "maximizing retirement contributions and paying off debts";
            case "Late Career":
                return "preparing for retirement and considering estate planning";
            default:
                return "reviewing your financial goals and adjusting your strategies accordingly";
        }
    }

    // Method to create a custom recommendation for a user of the same IFSC code
    public void createCustomRecommendation(Long bankerId, Long userId, String ifscCode, Recommendation recommendation) {
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (u:User)-[:BELONGS_TO]->(:PeerGroup {ifscCode: $ifscCode}) " +
                           "RETURN u.userId AS userId";

            // Set recommendation properties
            recommendation.setDateCreated(LocalDateTime.now());
            recommendation.setUserId(userId);
            recommendation.setType("custom"); // Mark as custom recommendation
                
            // Save recommendation to Neo4j
            recommendationRepository.save(recommendation);
        }
    }
}
