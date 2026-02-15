package org.harmony.transactionaggregator.controller;

import org.harmony.transactionaggregator.model.Transaction;
import org.harmony.transactionaggregator.service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class AggregatorController {

    private final AsyncService asyncService;

    @Autowired
    public AggregatorController(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    @GetMapping("/aggregate")
    public List<Transaction> aggregator(@RequestParam String account) {
        CompletableFuture<List<Transaction>> future1 =
                asyncService.fetchAsync("http://localhost:8888", account);
        CompletableFuture<List<Transaction>> future2 =
                asyncService.fetchAsync("http://localhost:8889", account);

        CompletableFuture.allOf(future1, future2).join();

        List<Transaction> allTransactions = new ArrayList<>();
        try {
            allTransactions.addAll(future1.get());
            allTransactions.addAll(future2.get());
        } catch (Exception e) {
            // Handle silently
        }

        allTransactions.sort(
                Comparator.comparing(Transaction::getTimestamp).reversed()
        );

        return allTransactions;
    }
}