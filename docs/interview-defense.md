# Interview Defense

## Why does idempotency matter?

Payment clients retry. Without idempotency, a timeout can become a duplicate payment or duplicate downstream processing.

## Why guard state transitions?

Payment state is not normal CRUD. A late or contradictory event must not rewrite a more final state.

## Why is duplicate webhook handling important?

Providers retry callbacks. If the system cannot ignore duplicates safely, it will create repeated effects.

## Why do settlement and reconciliation both exist?

Settlement groups successful transactions for payout logic. Reconciliation checks whether internal records still match provider-facing records after processing.

## Why does manual review exist?

Not every mismatch should be auto-fixed. Some cases need a queue and an explicit operator action.

## Why keep this as a modular monolith?

The goal is to go deep on correctness, tests, and failure rules before introducing distributed-system overhead.

## Why no real payment provider?

The point is to show reliability logic safely. A fake provider is enough to prove state handling, retries, and mismatches without operational or compliance risk.

## Why no cloud deployment?

This repo is meant to prove backend judgment on a laptop. Cloud setup would add noise without strengthening the core reliability argument.
