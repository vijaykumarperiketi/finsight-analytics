package com.finsightanalytics.analyticsservice.controller;

import com.finsightanalytics.common.model.Analytics;
import com.finsightanalytics.analyticsservice.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<List<Analytics>> getAllAnalytics() {
        return ResponseEntity.ok(analyticsService.getAllAnalytics());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Analytics>> getAnalyticsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getAnalyticsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Analytics> getAnalyticsById(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getAnalyticsById(id));
    }

    @PostMapping
    public ResponseEntity<Analytics> createAnalytics(@RequestBody Analytics analytics) {
        return ResponseEntity.ok(analyticsService.createAnalytics(analytics));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Analytics> updateAnalytics(@PathVariable Long id, @RequestBody Analytics analytics) {
        analytics.setId(id);
        return ResponseEntity.ok(analyticsService.updateAnalytics(analytics));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalytics(@PathVariable Long id) {
        analyticsService.deleteAnalytics(id);
        return ResponseEntity.noContent().build();
    }
}
