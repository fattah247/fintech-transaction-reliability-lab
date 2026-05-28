# 001 Use Monolith First

## Context

The project needs to prove payment reliability rules, not distributed systems orchestration.

## Decision

Use a modular monolith with clear package boundaries.

## Alternatives considered

- microservices
- event-driven split services

## Tradeoffs

The design is less visually flashy than microservices, but easier to test deeply and explain honestly.

## Consequences

The repo stays small enough to run locally and still show serious backend judgment.
