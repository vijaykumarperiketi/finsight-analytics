package com.finsightanalytics.common.client;

import com.finsightanalytics.common.model.Transaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "transaction-service", url = "${feign.client.transaction-service.url}")
public interface TransactionClient {

    @GetMapping("/transactions")
    List<Transaction> getAllTransactions();
}
