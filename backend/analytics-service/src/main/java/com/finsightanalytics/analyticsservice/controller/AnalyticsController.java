package com.finsightanalytics.analyticsservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finsightanalytics.analyticsservice.service.AnalyticsService;
import com.finsightanalytics.common.model.Analytics;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BANKER')")
    public ResponseEntity<List<Analytics>> getAllAnalyticsData() {
        List<Analytics> analyticsData = analyticsService.getAllAnalytics();
        return ResponseEntity.ok(analyticsData);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BANKER', 'CUSTOMER')")
    public ResponseEntity<List<Analytics>> getAnalyticsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getAnalyticsByUserId(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BANKER')")
    public ResponseEntity<Analytics> getAnalyticsById(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getAnalyticsById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BANKER')")
    public ResponseEntity<Analytics> createAnalytics(@RequestBody Analytics analytics) {
        return ResponseEntity.ok(analyticsService.createAnalytics(analytics));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BANKER')")
    public ResponseEntity<Analytics> updateAnalytics(@PathVariable Long id, @RequestBody Analytics analytics) {
        analytics.setAnalyticsId(id);
        return ResponseEntity.ok(analyticsService.updateAnalytics(analytics));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BANKER')")
    public ResponseEntity<Void> deleteAnalytics(@PathVariable Long id) {
        analyticsService.deleteAnalytics(id);
        return ResponseEntity.noContent().build();
    }
}
