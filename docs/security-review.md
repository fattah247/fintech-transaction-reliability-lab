# Security Review

## Idempotency abuse

Risk:
An attacker or buggy client replays payment creation requests.

Example attack:
The same request is sent multiple times after a timeout.

Impact:
Duplicate intents or duplicated downstream processing.

Mitigation in this repo:
Same `Idempotency-Key` and same payload returns the existing intent. A changed payload with the same key is rejected.

Remaining limitation:
The key is request-scoped and not rate-limited.

## Webhook spoofing

Risk:
An attacker sends a forged provider callback.

Example attack:
A fake success webhook is posted to the webhook endpoint.

Impact:
State corruption or false transaction success.

Mitigation in this repo:
The fake provider endpoint requires a configured signature and rejects invalid signatures.

Remaining limitation:
The signature is a simple local demo secret, not a real HMAC implementation.

## Replay attack

Risk:
The same webhook is delivered more than once.

Example attack:
The provider retries an event after a slow response.

Impact:
Duplicate transaction rows or repeated state transitions.

Mitigation in this repo:
Provider event id and provider transaction id are checked before processing.

Remaining limitation:
The system uses synchronous processing and does not model distributed replay defense.

## Invalid state transition

Risk:
Late or contradictory events rewrite already-final states.

Example attack:
A failed event arrives after success.

Impact:
State regression and broken settlement logic.

Mitigation in this repo:
The state machine rejects invalid transitions and creates a review case.

Remaining limitation:
No external rule engine or operator override flow is implemented.

## Refund abuse

Risk:
Refunds are requested after reconciliation without additional control.

Example attack:
An operator tries to refund a reconciled transaction directly.

Impact:
Operational inconsistency or accounting drift.

Mitigation in this repo:
Refund requests are blocked after reconciliation and require manual review.

Remaining limitation:
No formal approval workflow exists yet.

## Audit log integrity

Risk:
Important changes happen without a trace.

Example attack:
A transaction changes state and no audit event explains the reason.

Impact:
Poor incident response and weak compliance posture.

Mitigation in this repo:
State changes, mismatches, review resolution, and idempotency replays are recorded as audit events.

Remaining limitation:
Audit records are stored in the same database and are not immutable.
