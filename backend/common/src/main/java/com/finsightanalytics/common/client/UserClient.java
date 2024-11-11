package com.finsightanalytics.common.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.finsightanalytics.common.model.User;

@FeignClient(name = "user-management", url = "${feign.client.user-management.url}")
public interface UserClient {

    @GetMapping("/users")
    List<User> getAllUsers();
}
