package com.finsightanalytics.adminservice.service;

import com.finsightanalytics.common.client.AnalyticsClient;
import com.finsightanalytics.common.client.TransactionClient;
import com.finsightanalytics.common.client.UserClient;
import com.finsightanalytics.common.model.User;
import com.finsightanalytics.common.model.Transaction;
import com.finsightanalytics.common.model.Analytics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private TransactionClient transactionClient;

    @Autowired
    private AnalyticsClient analyticsClient;

    @Cacheable(value = "users", key = "#page + '-' + #size")
    public Page<User> getAllUsers(int page, int size) {
        List<User> users = userClient.getAllUsers();
        return paginate(users, page, size);
    }

    @Cacheable(value = "transactions", key = "#page + '-' + #size")
    public Page<Transaction> getAllTransactions(int page, int size) {
        List<Transaction> transactions = transactionClient.getAllTransactions();
        return paginate(transactions, page, size);
    }

    @Cacheable(value = "analytics", key = "#page + '-' + #size")
    public Page<Analytics> getAllAnalytics(int page, int size) {
        List<Analytics> analytics = analyticsClient.getAllAnalytics();
        return paginate(analytics, page, size);
    }

    private <T> Page<T> paginate(List<T> list, int page, int size) {
        int start = (int) PageRequest.of(page, size).getOffset();
        int end = Math.min((start + size), list.size());
        List<T> sublist = list.subList(start, end);
        return new PageImpl<>(sublist, PageRequest.of(page, size), list.size());
    }
}
