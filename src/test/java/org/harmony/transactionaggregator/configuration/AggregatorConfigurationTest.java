package org.harmony.transactionaggregator.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AggregatorConfigurationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private Executor taskExecutor;

    @Test
    @DisplayName("RestTemplate bean should be loaded")
    void restTemplateBeanShouldBeLoaded() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    @DisplayName("CacheManager bean should be loaded with 'transactions' cache")
    void cacheManagerBeanShouldBeLoaded() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCache("transactions")).isNotNull();
    }

    @Test
    @DisplayName("TaskExecutor bean should be loaded")
    void taskExecutorBeanShouldBeLoaded() {
        assertThat(taskExecutor).isNotNull();
    }
}
