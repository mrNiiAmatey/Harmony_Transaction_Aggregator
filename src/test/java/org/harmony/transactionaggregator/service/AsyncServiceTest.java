package org.harmony.transactionaggregator.service;

import org.harmony.transactionaggregator.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncServiceTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AsyncService asyncService;

    @Test
    @DisplayName("Should delegate fetch to TransactionService and return CompletableFuture")
    void shouldDelegateToTransactionService() throws Exception {
        List<Transaction> expected = List.of(
                new Transaction("txn-1", "server-1", "ACC-001", "100.00", "2025-02-15T10:00:00")
        );

        when(transactionService.fetchTransactions("http://localhost:8888", "ACC-001"))
                .thenReturn(expected);

        CompletableFuture<List<Transaction>> result =
                asyncService.fetchAsync("http://localhost:8888", "ACC-001");

        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getId()).isEqualTo("txn-1");
        verify(transactionService, times(1)).fetchTransactions("http://localhost:8888", "ACC-001");
    }

    @Test
    @DisplayName("Should return empty list in future when service returns empty")
    void shouldReturnEmptyFuture() throws Exception {
        when(transactionService.fetchTransactions(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        CompletableFuture<List<Transaction>> result =
                asyncService.fetchAsync("http://localhost:8889", "ACC-002");

        assertThat(result.get()).isEmpty();
    }
}
