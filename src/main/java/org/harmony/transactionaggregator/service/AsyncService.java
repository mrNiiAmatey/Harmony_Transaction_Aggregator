package org.harmony.transactionaggregator.service;

import org.harmony.transactionaggregator.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncService {

    private final TransactionService transactionService;

    @Autowired
    public AsyncService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Async
    public CompletableFuture<List<Transaction>> fetchAsync(String baseUrl, String account) {
        List<Transaction> transactions = transactionService.fetchTransactions(baseUrl, account);
        return CompletableFuture.completedFuture(transactions);
    }
}
