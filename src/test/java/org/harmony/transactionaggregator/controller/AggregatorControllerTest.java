package org.harmony.transactionaggregator.controller;

import org.harmony.transactionaggregator.model.Transaction;
import org.harmony.transactionaggregator.service.AsyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AggregatorController.class)
class AggregatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsyncService asyncService;

    @Test
    @DisplayName("Should return merged transactions sorted by timestamp descending")
    void shouldReturnMergedAndSortedTransactions() throws Exception {
        List<Transaction> server1 = List.of(
                new Transaction("txn-1", "server-1", "ACC-001", "100.00", "2025-02-13T08:00:00"),
                new Transaction("txn-2", "server-1", "ACC-001", "200.00", "2025-02-15T12:00:00")
        );
        List<Transaction> server2 = List.of(
                new Transaction("txn-3", "server-2", "ACC-001", "50.00", "2025-02-14T10:00:00")
        );

        when(asyncService.fetchAsync(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(server1))
                .thenReturn(CompletableFuture.completedFuture(server2));

        mockMvc.perform(get("/aggregate").param("account", "ACC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value("txn-2"))
                .andExpect(jsonPath("$[1].id").value("txn-3"))
                .andExpect(jsonPath("$[2].id").value("txn-1"));
    }

    @Test
    @DisplayName("Should return empty list when both services return no data")
    void shouldReturnEmptyWhenNoTransactions() throws Exception {
        when(asyncService.fetchAsync(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        mockMvc.perform(get("/aggregate").param("account", "ACC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return transactions from one source when the other is empty")
    void shouldHandleOneEmptySource() throws Exception {
        List<Transaction> server1 = List.of(
                new Transaction("txn-1", "server-1", "ACC-001", "300.00", "2025-02-15T14:00:00")
        );

        when(asyncService.fetchAsync(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(server1))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        mockMvc.perform(get("/aggregate").param("account", "ACC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value("300.00"));
    }

    @Test
    @DisplayName("Should return 400 when account parameter is missing")
    void shouldReturn400WhenAccountMissing() throws Exception {
        mockMvc.perform(get("/aggregate"))
                .andExpect(status().isBadRequest());
    }
}
