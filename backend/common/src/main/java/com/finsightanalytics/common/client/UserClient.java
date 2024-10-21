package com.finsightanalytics.common.client;

import com.finsightanalytics.common.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-management", url = "${feign.client.user-management.url}")
public interface UserClient {

    @GetMapping("/users")
    List<User> getAllUsers();
}
