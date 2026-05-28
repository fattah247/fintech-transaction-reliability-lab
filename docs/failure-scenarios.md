# Failure Scenarios

## Duplicate payment request

What happened:
The client retries payment creation with the same `Idempotency-Key`.

Why it matters:
Retries are normal in unreliable networks. Payments must not duplicate.

Expected behavior:
The same request returns the same payment intent. A different payload with the same key is rejected.

Proof:
- `shouldReturnSamePaymentIntentForSameIdempotencyKey()`
- `shouldRejectSameIdempotencyKeyWithDifferentPayload()`

## Duplicate webhook

What happened:
The fake provider sends the same success event twice.

Why it matters:
Real providers retry callbacks when they do not trust delivery.

Expected behavior:
The second event is ignored. The payment remains successful. A review case records the replay.

Proof:
- `shouldIgnoreDuplicateSuccessWebhook()`

## Contradictory late webhook

What happened:
A failed webhook arrives after the payment is already successful.

Why it matters:
Late or out-of-order provider events can corrupt state if the state machine is weak.

Expected behavior:
The event is ignored, the payment stays successful, and a review case records the invalid transition.

Proof:
- `shouldRejectInvalidTransactionStateTransition()`

## Webhook amount mismatch

What happened:
The provider callback amount does not match the local payment intent.

Why it matters:
Amount mismatches can signal tampering, provider bugs, or incorrect local records.

Expected behavior:
The payment is moved to manual review and the mismatch is audited.

Proof:
- `shouldCreateManualReviewForAmountMismatch()`

## Provider report mismatch

What happened:
The settlement report amount or settlement date differs from local records.

Why it matters:
Settlement and reconciliation are where real money movement is verified.

Expected behavior:
A manual review case is created for each mismatch and an audit event records the issue.

Proof:
- `shouldCreateManualReviewForReconciliationMismatch()`

## Refund after reconciliation

What happened:
A refund is requested after the transaction has already been reconciled.

Why it matters:
Post-reconciliation refunds are higher risk and often require explicit operator review.

Expected behavior:
The direct refund request is blocked and must go through manual review.

Proof:
- `shouldPreventRefundAfterReconciliationWithoutReview()`
