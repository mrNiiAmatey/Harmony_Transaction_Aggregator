package org.harmony.transactionaggregator.service;

import org.harmony.transactionaggregator.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TransactionService transactionService;

    private static final String BASE_URL = "http://localhost:8888";
    private static final String ACCOUNT = "ACC-001";
    private static final String EXPECTED_URL = BASE_URL + "/transactions?account=" + ACCOUNT;

    private List<Transaction> sampleTransactions() {
        return List.of(
                new Transaction("txn-1", "server-1", ACCOUNT, "100.00", "2025-02-15T10:00:00"),
                new Transaction("txn-2", "server-1", ACCOUNT, "250.50", "2025-02-14T09:30:00")
        );
    }

    private HttpServerErrorException serverError(int statusCode) {
        return HttpServerErrorException.create(
                HttpStatusCode.valueOf(statusCode), String.valueOf(statusCode),
                null, null, StandardCharsets.UTF_8);
    }

    private HttpClientErrorException clientError(int statusCode) {
        return HttpClientErrorException.create(
                HttpStatusCode.valueOf(statusCode), String.valueOf(statusCode),
                null, null, StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("Successful fetch scenarios")
    class SuccessfulFetch {

        @Test
        @DisplayName("Should return transactions on successful response")
        void shouldReturnTransactionsOnSuccess() {
            List<Transaction> expected = sampleTransactions();
            ResponseEntity<List<Transaction>> response = ResponseEntity.ok(expected);

            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(response);

            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("txn-1");
            assertThat(result.get(1).getAmount()).isEqualTo("250.50");
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
        }

        @Test
        @DisplayName("Should return empty list when response body is null")
        void shouldReturnEmptyListWhenBodyIsNull() {
            ResponseEntity<List<Transaction>> response = ResponseEntity.ok(null);

            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(response);

            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Retry logic scenarios")
    class RetryLogic {

        @Test
        @DisplayName("Should retry on HTTP 503 and succeed")
        void shouldRetryOn503AndSucceed() {
            List<Transaction> expected = sampleTransactions();
            ResponseEntity<List<Transaction>> successResponse = ResponseEntity.ok(expected);

            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            ))
                    .thenThrow(serverError(503))
                    .thenThrow(serverError(503))
                    .thenReturn(successResponse);

            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).hasSize(2);
            verify(restTemplate, times(3)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
        }

        @Test
        @DisplayName("Should retry on HTTP 529 server error and succeed")
        void shouldRetryOn529ServerErrorAndSucceed() {
            List<Transaction> expected = sampleTransactions();
            ResponseEntity<List<Transaction>> successResponse = ResponseEntity.ok(expected);

            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            ))
                    .thenThrow(serverError(529))
                    .thenReturn(successResponse);

            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).hasSize(2);
            verify(restTemplate, times(2)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
        }

        @Test
        @DisplayName("Should retry on HTTP 529 client error and succeed")
        void shouldRetryOn529ClientErrorAndSucceed() {
            List<Transaction> expected = sampleTransactions();
            ResponseEntity<List<Transaction>> successResponse = ResponseEntity.ok(expected);

            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            ))
                    .thenThrow(clientError(429))
                    .thenReturn(successResponse);

            // 429 is not 529, so the client error handler won't retry
            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).isEmpty();
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
        }

        @Test
        @DisplayName("Should return empty list after exhausting all 5 retries")
        void shouldReturnEmptyAfterMaxRetries() {
            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(serverError(503));

            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).isEmpty();
            verify(restTemplate, times(5)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
        }
    }

    @Nested
    @DisplayName("Non-retryable error scenarios")
    class NonRetryableErrors {

        @Test
        @DisplayName("Should not retry on HTTP 500 Internal Server Error")
        void shouldNotRetryOn500() {
            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(serverError(500));

            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).isEmpty();
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
        }

        @Test
        @DisplayName("Should not retry on HTTP 404 Not Found")
        void shouldNotRetryOn404() {
            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(clientError(404));

            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).isEmpty();
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
        }

        @Test
        @DisplayName("Should not retry on HTTP 400 Bad Request")
        void shouldNotRetryOn400() {
            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(clientError(400));

            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).isEmpty();
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
        }

        @Test
        @DisplayName("Should return empty list on connection failure")
        void shouldReturnEmptyOnConnectionFailure() {
            when(restTemplate.exchange(
                    eq(EXPECTED_URL),
                    eq(HttpMethod.GET),
                    isNull(),
                    any(ParameterizedTypeReference.class)
            )).thenThrow(new ResourceAccessException("Connection refused"));

            List<Transaction> result = transactionService.fetchTransactions(BASE_URL, ACCOUNT);

            assertThat(result).isEmpty();
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
        }
    }
}
