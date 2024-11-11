package com.finsightanalytics.common.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.finsightanalytics.common.model.Transaction;

@FeignClient(name = "transaction-service", url = "${feign.client.transaction-service.url}")
public interface TransactionClient {

    @GetMapping("/transactions")
    List<Transaction> getAllTransactions();
}
