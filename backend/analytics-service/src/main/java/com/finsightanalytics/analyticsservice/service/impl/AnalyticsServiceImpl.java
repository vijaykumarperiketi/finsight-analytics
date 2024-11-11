package com.finsightanalytics.analyticsservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finsightanalytics.analyticsservice.repository.AnalyticsRepository;
import com.finsightanalytics.analyticsservice.service.AnalyticsService;
import com.finsightanalytics.common.exception.AnalyticsDataNotFoundException;
import com.finsightanalytics.common.model.Analytics;
import com.finsightanalytics.common.model.AnalyticsData;
import com.finsightanalytics.common.model.Transaction;
import com.finsightanalytics.common.model.TransactionType;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Transactional
    @Override
    public Analytics createAnalytics(Analytics analytics) {
        // Set the timestamp for record creation
        analytics.setTimestamp(LocalDateTime.now());
        return analyticsRepository.save(analytics);
    }

    @Override
    public List<Analytics> getAllAnalytics() {
        return analyticsRepository.findAll();
    }

    @Override
    public List<Analytics> getAnalyticsByUserId(Long userId) {
        return analyticsRepository.findByUserId(userId);
    }

    @Override
    public Analytics getAnalyticsById(Long id) {
        return analyticsRepository.findById(id)
                .orElseThrow(() -> new AnalyticsDataNotFoundException("Analytics data not found with id: " + id));
    }

    @Override
    public Analytics updateAnalytics(Analytics analytics) {
        if (!analyticsRepository.existsById(analytics.getAnalyticsId())) {
            throw new AnalyticsDataNotFoundException("Analytics data not found with id: " + analytics.getAnalyticsId());
        }
        // Updating timestamp to reflect changes
        analytics.setTimestamp(LocalDateTime.now());
        return analyticsRepository.save(analytics);
    }

    @Override
    public void deleteAnalytics(Long id) {
        if (!analyticsRepository.existsById(id)) {
            throw new AnalyticsDataNotFoundException("Analytics data not found with id: " + id);
        }
        analyticsRepository.deleteById(id);
    }

    /**
     * Processes a transaction and updates the analytics for the user.
     * If no analytics exist for the user, a new one is created.
     *
     * @param transaction The transaction to process
     */
    @Override
    public void processTransaction(Transaction transaction) {
        Long userId = transaction.getUserId();
        
        // Find analytics for the user, or create a new one if it doesn't exist
        Analytics analytics = analyticsRepository.findByUserId(userId).stream().findFirst().orElse(new Analytics());
        analytics.setUserId(userId);
        analytics.setTimestamp(LocalDateTime.now());

        // Update the analytics data with the transaction details
        AnalyticsData analyticsData = analytics.getData() != null ? analytics.getData() : new AnalyticsData();
        
        if (transaction.getType() == TransactionType.CREDIT) {
            analyticsData.setTotalAmountSpent(analyticsData.getTotalAmountSpent() + transaction.getAmount().doubleValue());
            analyticsData.setCreditCount(analyticsData.getCreditCount() + 1);
        } else if (transaction.getType() == TransactionType.DEBIT) {
            analyticsData.setTotalAmountSpent(analyticsData.getTotalAmountSpent() - transaction.getAmount().doubleValue());
            analyticsData.setDebitCount(analyticsData.getDebitCount() + 1);
        }
        
        // Update other analytics information
        analyticsData.setTransactionCount(analyticsData.getTransactionCount() + 1);
        analyticsData.setAverageTransactionAmount(analyticsData.getTotalAmountSpent() / analyticsData.getTransactionCount());
        analyticsData.setLastTransactionDate(LocalDateTime.now());

        analytics.setData(analyticsData);

        // Save the updated analytics back to the database
        analyticsRepository.save(analytics);
    }
}
