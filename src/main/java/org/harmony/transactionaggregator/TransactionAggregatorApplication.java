package org.harmony.transactionaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableCaching
public class TransactionAggregatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionAggregatorApplication.class, args);
    }
}

