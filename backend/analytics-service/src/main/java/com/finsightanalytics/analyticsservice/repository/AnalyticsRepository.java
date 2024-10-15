package com.finsightanalytics.analyticsservice.repository;

import com.finsightanalytics.common.model.AnalyticsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsData, Long> {
    List<AnalyticsData> findByUserId(Long userId);
    List<AnalyticsData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
