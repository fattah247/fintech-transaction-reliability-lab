# 008 Why No Cloud Deployment

## Context

The project goal is to prove backend judgment without adding billing risk or cloud setup noise.

## Decision

Keep the repo local-first and use a public repository only for source hosting and test execution.

## Alternatives considered

- deploy to a free tier
- add staging infrastructure

## Tradeoffs

No public preview exists, but the technical discussion stays focused on reliability instead of platform wiring.

## Consequences

The repo remains cheap, safe, and easy to hand off.
