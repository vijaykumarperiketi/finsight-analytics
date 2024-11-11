package com.finsightanalytics.common.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.finsightanalytics.common.model.Analytics;

@FeignClient(name = "analytics-service", url = "${feign.client.analytics-service.url}")
public interface AnalyticsClient {

    @GetMapping("/analytics")
    List<Analytics> getAllAnalyticsData();
}
