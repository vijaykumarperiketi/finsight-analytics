package com.example.analyticsservice.service;

import com.example.common.exception.AnalyticsDataNotFoundException;
import com.example.common.model.Analytics;
import com.example.common.model.AnalyticsData;
import com.example.common.model.Transaction;
import com.example.analyticsservice.repository.AnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Transactional
    @Override
    public Analytics createAnalytics(Analytics analytics) {
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
        return analyticsRepository.findById(id).orElseThrow(() -> new AnalyticsDataNotFoundException("Analytics data not found with id: " + id));
    }

    @Override
    public Analytics updateAnalytics(Analytics analytics) {
        if (!analyticsRepository.existsById(analytics.getId())) {
            throw new AnalyticsDataNotFoundException("Analytics data not found with id: " + analytics.getId());
        }
        return analyticsRepository.save(analytics);
    }

    @Override
    public void deleteAnalytics(Long id) {
        if (!analyticsRepository.existsById(id)) {
            throw new AnalyticsDataNotFoundException("Analytics data not found with id: " + id);
        }
        analyticsRepository.deleteById(id);
    }

    private void processTransaction(Transaction transaction) {
        Long userId = transaction.getUserId();
        Analytics analytics = analyticsRepository.findByUserId(userId).stream().findFirst().orElse(new Analytics());
        
        analytics.setUserId(userId);
        analytics.setTimestamp(LocalDateTime.now());

        AnalyticsData analyticsData = analytics.getData() != null ? analytics.getData() : new AnalyticsData();

        if ("credit".equalsIgnoreCase(transaction.getType())) {
            analyticsData.setTotalAmountSpent(analyticsData.getTotalAmountSpent() + transaction.getAmount().doubleValue());
            analyticsData.setCreditCount(analyticsData.getCreditCount() + 1);
        } else if ("debit".equalsIgnoreCase(transaction.getType())) {
            analyticsData.setTotalAmountSpent(analyticsData.getTotalAmountSpent() - transaction.getAmount().doubleValue());
            analyticsData.setDebitCount(analyticsData.getDebitCount() + 1);
        }
        analyticsData.setTransactionCount(analyticsData.getTransactionCount() + 1);
        analyticsData.setAverageTransactionAmount(analyticsData.getTotalAmountSpent() / analyticsData.getTransactionCount());
        analyticsData.setLastTransactionDate(LocalDateTime.now());

        analytics.setData(analyticsData);
        createAnalytics(analytics);
    }
}
