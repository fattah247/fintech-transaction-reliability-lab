# Fintech Transaction Reliability Lab

This project simulates common reliability problems in payment systems: duplicate requests, webhook retries, invalid state changes, settlement mismatches, refund timing, manual review, and auditability.

It is a local-first modular monolith built to show backend judgment, not cloud setup or framework sprawl.

> This is a public-safe educational project based on common payment engineering patterns. It does not use proprietary employer code, architecture, data, naming, business rules, logs, screenshots, or internal workflows.

## What This Proves

- payment state management
- idempotency handling
- duplicate webhook handling
- settlement batch creation
- reconciliation mismatch detection
- auditability
- failure handling
- backend engineering judgment

## What It Simulates

- payment intent creation
- guarded transaction state transitions
- fake provider authorization, success, failure, expiry, and reversal events
- duplicate and contradictory webhooks
- settlement batches for successful transactions
- reconciliation against provider-style reports
- manual review creation and resolution
- refund request and completion flow

## Architecture

```text
Laptop
├── JDK 21
├── Apache Maven
├── Spring Boot backend app
├── Docker Compose
│   └── PostgreSQL local container
├── H2 test database
├── Git local repository
└── Public GitHub repository
    └── GitHub Actions test workflow
```

## Why It Stays Small

This project does not try to look senior by adding microservices, Kafka, cloud deployment, or managed infrastructure. The goal is to go deep on state correctness and failure handling with one Spring Boot app that can be tested honestly on a laptop.

## State Model

The payment state machine is centered on correctness:

```text
CREATED
PENDING
AUTHORIZED
SUCCESS
FAILED
EXPIRED
REVERSAL_REQUIRED
SETTLED
RECONCILED
REFUND_REQUESTED
REFUNDED
MANUAL_REVIEW
```

Examples of guarded rules:

- `SUCCESS` cannot go back to `PENDING`
- `FAILED` cannot become `SUCCESS` through a contradictory late event
- `SETTLED` is required before `RECONCILED`
- `RECONCILED` blocks direct refund requests
- duplicate provider events do not create duplicate transactions

## Project Layout

```text
.
├── .github/workflows/ci.yml
├── docs
│   ├── adr
│   ├── demo-flow.md
│   ├── failure-scenarios.md
│   ├── interview-defense.md
│   └── security-review.md
├── scripts/demo.sh
├── src/main/java/id/fatarc/portfolio/fintechlab
│   ├── audit
│   ├── common
│   ├── payment
│   ├── reconciliation
│   ├── refund
│   ├── review
│   └── settlement
└── src/test/java/id/fatarc/portfolio/fintechlab/payment
```

## Local Setup

Prerequisites:

- JDK 21
- Maven 3.9+
- Docker

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the app:

```bash
mvn spring-boot:run
```

Run tests:

```bash
mvn test
```

Wrapper fallback:

```bash
./mvnw spring-boot:run
./mvnw test
```

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Endpoints

Payment intents:

- `POST /api/payment-intents`
- `POST /api/payment-intents/{paymentIntentId}/start`
- `GET /api/payment-intents/{paymentIntentId}`

Fake provider:

- `POST /fake-provider/webhook`

Settlement:

- `POST /api/settlements/batches?merchantId=<merchant-id>`
- `GET /api/settlements/batches`
- `GET /api/settlements/batches/{batchId}/items`

Reconciliation:

- `POST /api/reconciliation/reports`

Audit and review:

- `GET /api/audit-events?aggregateType=PAYMENT_INTENT&aggregateId=<id>`
- `GET /api/review-cases`
- `POST /api/review-cases/{reviewCaseId}/resolve`

Refunds:

- `POST /api/refunds/payment-intents/{paymentIntentId}`
- `POST /api/refunds/{refundId}/complete`

## Example Flow

Create a payment intent:

```bash
curl -X POST http://localhost:8080/api/payment-intents \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: payment-001' \
  -d '{
    "merchantId": "merchant-demo-01",
    "merchantReference": "ORDER-1001",
    "amount": 125000,
    "currency": "IDR"
  }'
```

Start execution:

```bash
curl -X POST http://localhost:8080/api/payment-intents/<payment-intent-id>/start
```

Send a fake provider success webhook:

```bash
curl -X POST http://localhost:8080/fake-provider/webhook \
  -H 'Content-Type: application/json' \
  -H 'X-Provider-Signature: local-demo-signature' \
  -d '{
    "providerEventId": "evt-001",
    "providerTransactionId": "provider-tx-001",
    "paymentIntentId": "<payment-intent-id>",
    "status": "SUCCESS",
    "amount": 125000,
    "currency": "IDR"
  }'
```

Create a settlement batch:

```bash
curl -X POST 'http://localhost:8080/api/settlements/batches?merchantId=merchant-demo-01'
```

See the full flow in [docs/demo-flow.md](/Users/muhammadfattah/Documents/Projects/Git/Active/fintech-transaction-reliability-lab/docs/demo-flow.md) or run [scripts/demo.sh](/Users/muhammadfattah/Documents/Projects/Git/Active/fintech-transaction-reliability-lab/scripts/demo.sh).

## What The Tests Prove

The tests focus on business invariants, not only `200 OK` responses.

Current coverage includes:

- same idempotency key returns the original payment intent
- same idempotency key with a different payload is rejected
- missing idempotency key is rejected
- duplicate success webhook is ignored
- contradictory late webhook does not rewrite a successful payment
- amount mismatch creates manual review
- every state change writes an audit event
- settlement marks a payment as settled
- matching reconciliation moves a payment to reconciled
- mismatched reconciliation creates review cases
- refund after reconciliation is blocked

## Documentation Map

- [failure-scenarios.md](/Users/muhammadfattah/Documents/Projects/Git/Active/fintech-transaction-reliability-lab/docs/failure-scenarios.md)
- [security-review.md](/Users/muhammadfattah/Documents/Projects/Git/Active/fintech-transaction-reliability-lab/docs/security-review.md)
- [interview-defense.md](/Users/muhammadfattah/Documents/Projects/Git/Active/fintech-transaction-reliability-lab/docs/interview-defense.md)
- [demo-flow.md](/Users/muhammadfattah/Documents/Projects/Git/Active/fintech-transaction-reliability-lab/docs/demo-flow.md)
- [adr](/Users/muhammadfattah/Documents/Projects/Git/Active/fintech-transaction-reliability-lab/docs/adr)

## Continuous Verification

GitHub Actions is intentionally narrow:

- runs on `push`
- runs on `pull_request`
- uses the standard hosted runner
- runs `./mvnw test`

No deployment, registry publish, artifact publish, or scheduled workflow is included.

## Limitations

- This project does not process real payments.
- This project does not connect to a real payment gateway.
- This project does not store card data.
- This project does not implement PCI-DSS compliance.
- This project does not represent any employer system.
- This project does not use proprietary architecture, data, or naming.
- This project does not model distributed locking, queue-based retries, or multi-region behavior.

## Guardrails

Do not add these later unless the goal changes:

- real payment gateways
- managed databases
- cloud deployment
- hosted observability
- external notification services
- private CI runners
- image publishing workflows
- scheduled jobs
- hosted queues
- paid security scanners
- anything that needs billing setup
