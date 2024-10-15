package com.finsightanalytics.common.client;

import com.finsightanalytics.common.model.Recommendation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "recommendation-engine", url = "http://recommendation-engine.default.svc.cluster.local:8080")
public interface RecommendationClient {

    @GetMapping("/recommendations")
    List<Recommendation> getAllRecommendations(@RequestParam String role);
}
