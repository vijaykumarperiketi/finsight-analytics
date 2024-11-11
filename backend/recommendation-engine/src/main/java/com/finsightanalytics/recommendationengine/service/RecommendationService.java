package com.finsightanalytics.recommendationengine.service;

import java.util.List;

import com.finsightanalytics.common.model.RecommendationType;
import com.finsightanalytics.recommendationengine.model.Neo4jRecommendation;

public interface RecommendationService {

    // 1. Get recent recommendations for a user
    List<Neo4jRecommendation> fetchRecentRecommendations(Long userId);

    // 2. Get recommendations by type for a user
    List<Neo4jRecommendation> fetchRecommendationsByType(Long userId, RecommendationType type);

    // 3. Get all recommendations for a user
    List<Neo4jRecommendation> fetchAllRecommendations(Long userId);

    // 4. Create a custom recommendation
    Neo4jRecommendation createCustomRecommendation(Long bankerId, Long userId, Neo4jRecommendation recommendation);

    // Generate all recommendations for a user
    void generateRecommendationsForUser(Long userId);

    // Methods for generating different types of recommendations
    void generatePeerBasedRecommendations(Long userId);

    void generateGoalBasedRecommendations(Long userId);

    void generateDebtBasedRecommendations(Long userId);

    void generateTransactionBasedRecommendations(Long userId);

    void generateLifecycleBasedRecommendations(Long userId);
}
