# 002 Idempotency Strategy

## Context

Payment creation requests can be retried by clients or proxies.

## Decision

Persist an idempotency key on the payment intent and reject payload changes for reused keys.

## Alternatives considered

- no idempotency
- cache-only idempotency

## Tradeoffs

Database-backed idempotency is simple and durable, but ties replay behavior to persistence.

## Consequences

The repo can prove duplicate request safety with deterministic tests.
