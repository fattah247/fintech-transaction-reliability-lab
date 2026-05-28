# Demo Flow

## Goal

Show the project in a way that highlights reliability rules instead of CRUD screens.

## Before starting

1. Start PostgreSQL with `docker compose up -d postgres`
2. Run the app with `mvn spring-boot:run`
3. Open another terminal for `scripts/demo.sh`

## Demo steps

1. Create a payment intent with `Idempotency-Key: demo-key-001`
2. Repeat the same request with the same key
3. Start the payment
4. Send a fake provider success webhook
5. Create a settlement batch
6. Import a reconciliation report with an amount mismatch
7. List open manual review cases
8. Fetch the audit trail for the payment intent

## What to point out

- The repeated payment request returns the same payment intent instead of creating a duplicate.
- The provider webhook changes state through guarded transitions.
- Settlement only includes successful transactions that are not already settled.
- Reconciliation creates explicit mismatch records instead of silently overwriting state.
- The audit log shows who changed state, why, and from which state to which state.

## Scripted demo

Run:

```bash
./scripts/demo.sh
```
