# Harmony Transaction Aggregator Service

A high-performance **Spring Boot microservice** that aggregates financial transaction data from multiple upstream sources in real time. Built with a focus on resilience, concurrency, and production-readiness.

## Architecture Overview

```
Client Request
      │
      ▼
┌─────────────────────┐
│  Aggregator Controller │  ← REST API endpoint
└─────────┬───────────┘
          │
    ┌─────┴─────┐
    ▼           ▼
┌────────┐ ┌────────┐
│ Async  │ │ Async  │     ← Concurrent fetches via ThreadPoolTaskExecutor
│ Fetch  │ │ Fetch  │
└───┬────┘ └───┬────┘
    │          │
    ▼          ▼
┌────────┐ ┌────────┐
│ Bank A │ │ Bank B │     ← Upstream transaction services
└────────┘ └────────┘
```

## Key Features

**Asynchronous Processing** — Fetches transaction data from multiple upstream services concurrently using `CompletableFuture` and a configurable `ThreadPoolTaskExecutor`, minimizing total response time.

**Retry Logic with Status-Aware Error Handling** — Implements a configurable retry mechanism (up to 5 attempts) that intelligently distinguishes between retryable errors (HTTP 503, 529) and terminal failures, preventing unnecessary retries on client errors.

**In-Memory Caching** — Uses Spring's `@Cacheable` abstraction backed by `ConcurrentMapCacheManager` to cache responses per source and account, reducing redundant network calls.

**Clean Layered Architecture** — Separates concerns across Controller, Service, and Configuration layers following Spring best practices for maintainability and testability.

## Tech Stack

| Layer          | Technology                          |
|----------------|-------------------------------------|
| Framework      | Spring Boot 3.4                     |
| Language       | Java 21                             |
| HTTP Client    | RestTemplate                        |
| Async          | Spring @Async + CompletableFuture   |
| Caching        | Spring Cache (ConcurrentMapCache)   |
| Monitoring     | Spring Boot Actuator                |
| Build Tool     | Gradle 9.3                          |
| Testing        | JUnit 5 + Spring Boot Test          |

## Getting Started

### Prerequisites

- Java 21+
- Gradle 9.3+ (or use the included wrapper)

### Run the Application

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/Harmony_Transaction_Aggregator.git
cd Harmony_Transaction_Aggregator

# Build and run
./gradlew bootRun
```

The service starts on `http://localhost:8080` by default.

### API Usage

```bash
GET /aggregate?account={accountId}
```

**Example:**

```bash
curl http://localhost:8080/aggregate?account=ACC-001
```

**Response:** Returns a merged, reverse-chronologically sorted list of transactions from all upstream sources.

```json
[
  {
    "id": "txn-042",
    "serverId": "server-1",
    "account": "ACC-001",
    "amount": "250.00",
    "timestamp": "2025-02-15T14:30:00"
  },
  {
    "id": "txn-017",
    "serverId": "server-2",
    "account": "ACC-001",
    "amount": "89.99",
    "timestamp": "2025-02-14T09:15:00"
  }
]
```

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Configuration

The async thread pool is configurable in `AggregatorConfiguration.java`:

| Parameter       | Default | Description                        |
|-----------------|---------|------------------------------------|
| Core Pool Size  | 4       | Minimum threads kept alive         |
| Max Pool Size   | 8       | Maximum concurrent threads         |
| Queue Capacity  | 100     | Pending tasks before rejection     |
| Max Retries     | 5       | Retry attempts for failed fetches  |

## Project Structure

```
src/main/java/org/harmony/transactionaggregator/
├── TransactionAggregatorApplication.java   # Entry point
├── configuration/
│   └── AggregatorConfiguration.java        # Beans: RestTemplate, Cache, Async Executor
├── controller/
│   └── AggregatorController.java           # REST endpoint
├── model/
│   └── Transaction.java                    # Data model
└── service/
    ├── AsyncService.java                   # Async orchestration layer
    └── TransactionService.java             # HTTP client with retry + caching
```

## Roadmap

- [ ] Circuit breaker pattern (Resilience4j)
- [ ] Configurable upstream service URLs via `application.properties`
- [ ] Pagination support for large transaction sets
- [ ] OpenAPI/Swagger documentation
- [ ] Docker containerization
- [ ] Integration tests with WireMock

## License

This project is available for educational and portfolio purposes.
