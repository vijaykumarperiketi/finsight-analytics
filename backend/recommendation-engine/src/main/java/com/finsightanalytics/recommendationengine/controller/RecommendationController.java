package com.finsightanalytics.recommendationengine.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finsightanalytics.common.model.RecommendationType;
import com.finsightanalytics.recommendationengine.model.Neo4jRecommendation;
import com.finsightanalytics.recommendationengine.service.RecommendationService;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    @Autowired
    private RecommendationService recommendationService;

    // 1. Get recent recommendations for a user
    @GetMapping("/recent/{userId}")
    public ResponseEntity<List<Neo4jRecommendation>> getRecentRecommendations(@PathVariable Long userId) {
        try {
            List<Neo4jRecommendation> recommendations = recommendationService.fetchRecentRecommendations(userId);
            if (recommendations.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error fetching recent recommendations for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // 2. Get recommendations by type for a user
    @GetMapping("/type/{userId}/{type}")
    public ResponseEntity<List<Neo4jRecommendation>> getRecommendationsByType(@PathVariable Long userId,
                                                                              @PathVariable RecommendationType type) {
        try {
            List<Neo4jRecommendation> recommendations = recommendationService.fetchRecommendationsByType(userId, type);
            if (recommendations.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error fetching recommendations of type {} for user {}: {}", type, userId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // 3. Get all recommendations for a user
    @GetMapping("/all/{userId}")
    public ResponseEntity<List<Neo4jRecommendation>> getAllRecommendations(@PathVariable Long userId) {
        try {
            List<Neo4jRecommendation> recommendations = recommendationService.fetchAllRecommendations(userId);
            if (recommendations.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error fetching all recommendations for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // 4. Create a custom recommendation
    @PostMapping("/custom/{bankerId}/{userId}")
    public ResponseEntity<Neo4jRecommendation> createCustomRecommendation(@PathVariable Long bankerId,
                                                                          @PathVariable Long userId,
                                                                          @RequestBody Neo4jRecommendation recommendation) {
        try {
            Neo4jRecommendation createdRecommendation = recommendationService.createCustomRecommendation(bankerId, userId, recommendation);
            return ResponseEntity.ok(createdRecommendation);
        } catch (Exception e) {
            logger.error("Error creating custom recommendation for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
