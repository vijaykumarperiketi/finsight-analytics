package com.finsightanalytics.common.client;

import com.finsightanalytics.common.model.AnalyticsData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "analytics-service", url = "${feign.client.analytics-service.url}")
public interface AnalyticsClient {

    @GetMapping("/analytics")
    List<AnalyticsData> getAllAnalyticsData();
}
