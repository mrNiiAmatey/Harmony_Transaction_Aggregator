package org.harmony.transactionaggregator.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    @DisplayName("Should create transaction with all-args constructor")
    void shouldCreateTransactionWithAllArgs() {
        Transaction transaction = new Transaction("txn-1", "server-1", "ACC-001", "150.00", "2025-02-15T10:00:00");

        assertThat(transaction.getId()).isEqualTo("txn-1");
        assertThat(transaction.getServerId()).isEqualTo("server-1");
        assertThat(transaction.getAccount()).isEqualTo("ACC-001");
        assertThat(transaction.getAmount()).isEqualTo("150.00");
        assertThat(transaction.getTimestamp()).isEqualTo("2025-02-15T10:00:00");
    }

    @Test
    @DisplayName("Should create transaction with no-args constructor and setters")
    void shouldCreateTransactionWithSetters() {
        Transaction transaction = new Transaction();
        transaction.setId("txn-2");
        transaction.setServerId("server-2");
        transaction.setAccount("ACC-002");
        transaction.setAmount("75.25");
        transaction.setTimestamp("2025-02-14T09:30:00");

        assertThat(transaction.getId()).isEqualTo("txn-2");
        assertThat(transaction.getServerId()).isEqualTo("server-2");
        assertThat(transaction.getAccount()).isEqualTo("ACC-002");
        assertThat(transaction.getAmount()).isEqualTo("75.25");
        assertThat(transaction.getTimestamp()).isEqualTo("2025-02-14T09:30:00");
    }

    @Test
    @DisplayName("No-args constructor should have null fields by default")
    void shouldHaveNullFieldsByDefault() {
        Transaction transaction = new Transaction();

        assertThat(transaction.getId()).isNull();
        assertThat(transaction.getServerId()).isNull();
        assertThat(transaction.getAccount()).isNull();
        assertThat(transaction.getAmount()).isNull();
        assertThat(transaction.getTimestamp()).isNull();
    }
}