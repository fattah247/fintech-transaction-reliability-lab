# 006 Audit Log Design

## Context

Payment systems need an explanation for every important state change and review action.

## Decision

Record audit events with event type, previous state, new state, actor, reason, timestamp, and metadata.

## Alternatives considered

- plain application logs only
- minimal event type only

## Tradeoffs

The audit model is more detailed, but far easier to inspect during incidents.

## Consequences

The repo shows traceability instead of only control flow.
