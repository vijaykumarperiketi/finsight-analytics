package com.finsightanalytics.recommendationengine.controller;

import com.finsightanalytics.common.model.Recommendation;
import com.finsightanalytics.recommendationengine.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/transaction-based/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<List<Recommendation>> getTransactionBasedRecommendations(@PathVariable Long userId) {
        List<Recommendation> recommendations = recommendationService.generateTransactionBasedRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/financial-goal/{userId}/{goalId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<List<Recommendation>> getFinancialGoalRecommendations(@PathVariable Long userId, @PathVariable Long goalId) {
        List<Recommendation> recommendations = recommendationService.generateFinancialGoalRecommendations(userId, goalId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/debt-goal/{userId}/{debtId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<List<Recommendation>> getDebtManagementRecommendations(@PathVariable Long userId, @PathVariable Long debtId) {
        List<Recommendation> recommendations = recommendationService.generateDebtManagementRecommendations(userId, debtId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/peer-based/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<List<Recommendation>> getPeerBasedRecommendations(@PathVariable Long userId) {
        List<Recommendation> recommendations = recommendationService.generatePeerBasedRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/lifecycle-based/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<List<Recommendation>> getLifecycleBasedRecommendations(@PathVariable Long userId) {
        List<Recommendation> recommendations = recommendationService.generateLifecycleBasedRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/custom/{bankerId}/{userId}/{ifscCode}")
    @PreAuthorize("hasAnyRole('BANKER')")
    public ResponseEntity<Recommendation> createCustomRecommendation(@PathVariable Long bankerId, @PathVariable Long userId, @PathVariable String ifscCode, @RequestBody Recommendation recommendation) {
        recommendationService.createCustomRecommendation(bankerId, userId, ifscCode, recommendation);
        return ResponseEntity.ok(recommendation);
    }
}
