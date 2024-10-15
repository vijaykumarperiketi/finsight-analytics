package com.finsightanalytics.analyticsservice.service;

import com.finsightanalytics.common.model.Analytics;
import com.finsightanalytics.common.model.Transaction;

import java.util.List;

public interface AnalyticsService {
    List<Analytics> getAllAnalytics();
    List<Analytics> getAnalyticsByUserId(Long userId);
    Analytics getAnalyticsById(Long id);
    Analytics createAnalytics(Analytics analytics);
    Analytics updateAnalytics(Analytics analytics);
    void deleteAnalytics(Long id);
    void processTransaction(Transaction transaction);
}
