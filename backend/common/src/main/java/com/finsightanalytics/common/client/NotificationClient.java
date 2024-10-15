package com.finsightanalytics.common.client;

import com.finsightanalytics.common.model.Notification;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "notification-service", url = "http://notification-service.default.svc.cluster.local:8080")
public interface NotificationClient {

    @GetMapping("/notifications")
    List<Notification> getAllNotifications(@RequestParam String role);
}
