package com.finsightanalytics.common.model;

public enum RecommendationType {
    TRANSACTION_BASED,     // Recommendation based on transaction history
    GOAL_BASED,            // Recommendation related to financial goals
    PEER_BASED,            // Recommendation based on peer comparisons
    LIFECYCLE_BASED,       // Recommendation based on lifecycle events (e.g., nearing retirement)
    DEBT_BASED,            // Recommendation based on debts
    CUSTOM                 // Recommendation created by Banker
}
