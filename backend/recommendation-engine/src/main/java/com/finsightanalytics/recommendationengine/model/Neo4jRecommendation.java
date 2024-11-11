package com.finsightanalytics.recommendationengine.model;

import java.time.LocalDateTime;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.finsightanalytics.common.model.RecommendationType;

@Node("Recommendation")
public class Neo4jRecommendation {

    @Id
    @GeneratedValue
    private Long recommendationId;
    private Long userId;
    private RecommendationType type; // E.g., "transaction-based", "goal-based", etc.
    private String message;
    private LocalDateTime dateCreated;
    private Long bankerId;

    public Neo4jRecommendation() {}
    
    public Neo4jRecommendation(Long recommendationId, RecommendationType type, String message, LocalDateTime dateCreated) {
        this.recommendationId = recommendationId;
        this.type = type;
        this.message = message;
        this.dateCreated = dateCreated;
    }

    public Neo4jRecommendation(org.neo4j.driver.types.Node node) {
        this.recommendationId = node.containsKey("recommendationId") ? node.get("recommendationId").asLong() : null;
        this.userId = node.containsKey("userId") ? node.get("userId").asLong() : null;
        this.type = node.containsKey("type") ? RecommendationType.valueOf(node.get("type").asString()) : null;
        this.message = node.containsKey("message") ? node.get("message").asString() : null;
        this.dateCreated = node.containsKey("dateCreated") ? node.get("dateCreated").asLocalDateTime() : null;
        this.bankerId = node.containsKey("bankerId") ? node.get("bankerId").asLong() : null;
    }

    // Getters and setters
    public Long getRecommendationId() { return recommendationId; }
    public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }

    public RecommendationType getType() { return type; }
    public void setType(RecommendationType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }

    public Long getBankerId() { return bankerId; }
    public void setBankerId(Long bankerId) { this.bankerId = bankerId; }    

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
