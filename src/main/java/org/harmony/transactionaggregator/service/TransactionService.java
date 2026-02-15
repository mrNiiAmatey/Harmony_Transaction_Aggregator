package org.harmony.transactionaggregator.service;

import org.harmony.transactionaggregator.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final RestTemplate restTemplate;
    private static final int MAX_RETRIES = 5;

    @Autowired
    public TransactionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "transactions", key = "#baseUrl + '-' + #account")
    public List<Transaction> fetchTransactions(String baseUrl, String account) {
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                String url = baseUrl + "/transactions?account=" + account;
                ResponseEntity<List<Transaction>> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Transaction>>() {}
                        );

                return response.getBody() != null
                        ? response.getBody()
                        : new ArrayList<>();

            } catch (HttpServerErrorException e) {
                int statusCode = e.getStatusCode().value();
                if (statusCode != 529 && statusCode != 503) {
                    return new ArrayList<>();
                }
                attempt++;

            } catch (HttpClientErrorException e) {
                int statusCode = e.getStatusCode().value();
                if (statusCode != 529) {
                    return new ArrayList<>();
                }
                attempt++;

            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
}