# 003 Transaction State Machine

## Context

Payment systems fail when states are treated as loose strings instead of guarded business rules.

## Decision

Model explicit states and reject invalid transitions in the domain object.

## Alternatives considered

- update state directly in services
- store provider status without a local lifecycle

## Tradeoffs

The entity is slightly more opinionated, but the business rules are easier to defend.

## Consequences

Late, duplicate, or contradictory events cannot silently rewrite state.
