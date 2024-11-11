package com.finsightanalytics.common.model;

import java.time.LocalDateTime;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Analytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long analyticsId;

    private Long userId;

    @Embedded
    private AnalyticsData data;

    private LocalDateTime timestamp;

    // Getters and setters

    public Long getAnalyticsId() {
        return analyticsId;
    }

    public void setAnalyticsId(Long analyticsId) {
        this.analyticsId = analyticsId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AnalyticsData getData() {
        return data;
    }

    public void setData(AnalyticsData data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
