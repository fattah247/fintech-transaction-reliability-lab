#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
SIGNATURE="${SIGNATURE:-local-demo-signature}"
RUN_ID="$(date +%Y%m%d%H%M%S)"

KEY="demo-key-$RUN_ID"
MERCHANT_ID="merchant-demo-01"
ORDER_ID="ORDER-DEMO-$RUN_ID"
PROVIDER_EVENT_ID="evt-demo-$RUN_ID"
PROVIDER_TX_ID="provider-tx-demo-$RUN_ID"

json_field() {
  local field="$1"
  python3 -c 'import json,sys; print(json.load(sys.stdin)[sys.argv[1]])' "$field"
}

pretty_json() {
  python3 -m json.tool
}

section() {
  echo
  echo "============================================================"
  echo "$1"
  echo "============================================================"
}

clear

echo "Fintech Transaction Reliability Lab"
echo "Local demo run: $RUN_ID"
echo
echo "This demo proves:"
echo "- idempotency replay"
echo "- provider webhook processing"
echo "- settlement batch creation"
echo "- reconciliation mismatch handling"
echo "- manual review creation"
echo "- audit trail visibility"

section "1. Create payment intent"

create_response=$(curl -sS -X POST "$BASE_URL/api/payment-intents" \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $KEY" \
  -d "{
    \"merchantId\": \"$MERCHANT_ID\",
    \"merchantReference\": \"$ORDER_ID\",
    \"amount\": 125000,
    \"currency\": \"IDR\"
  }")

payment_intent_id=$(printf '%s' "$create_response" | json_field id)

echo "PaymentIntent ID:"
echo "$payment_intent_id"

section "2. Replay same request with same Idempotency-Key"

replay_response=$(curl -sS -X POST "$BASE_URL/api/payment-intents" \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $KEY" \
  -d "{
    \"merchantId\": \"$MERCHANT_ID\",
    \"merchantReference\": \"$ORDER_ID\",
    \"amount\": 125000,
    \"currency\": \"IDR\"
  }")

replay_id=$(printf '%s' "$replay_response" | json_field id)

echo "Original ID : $payment_intent_id"
echo "Replay ID   : $replay_id"

if [ "$payment_intent_id" = "$replay_id" ]; then
  echo "Result      : PASS - same PaymentIntent returned"
else
  echo "Result      : FAIL - different PaymentIntent returned"
fi

section "3. Start payment execution"

curl -sS -X POST "$BASE_URL/api/payment-intents/$payment_intent_id/start" | pretty_json

section "4. Send fake provider success webhook"

curl -sS -X POST "$BASE_URL/fake-provider/webhook" \
  -H 'Content-Type: application/json' \
  -H "X-Provider-Signature: $SIGNATURE" \
  -d "{
    \"providerEventId\": \"$PROVIDER_EVENT_ID\",
    \"providerTransactionId\": \"$PROVIDER_TX_ID\",
    \"paymentIntentId\": \"$payment_intent_id\",
    \"status\": \"SUCCESS\",
    \"amount\": 125000,
    \"currency\": \"IDR\"
  }" | pretty_json

section "5. Create settlement batch"

batch_response=$(curl -sS -X POST "$BASE_URL/api/settlements/batches?merchantId=$MERCHANT_ID")
batch_id=$(printf '%s' "$batch_response" | json_field id)

echo "$batch_response" | pretty_json

section "6. Import reconciliation report with mismatch"

curl -sS -X POST "$BASE_URL/api/reconciliation/reports" \
  -H 'Content-Type: application/json' \
  -d "{
    \"rows\": [
      {
        \"providerTransactionId\": \"$PROVIDER_TX_ID\",
        \"amount\": 124000,
        \"currency\": \"IDR\",
        \"providerStatus\": \"SUCCESS\",
        \"settlementDate\": \"2026-01-01\"
      }
    ]
  }" | pretty_json

section "7. Open review cases"

curl -sS "$BASE_URL/api/review-cases" | pretty_json

section "8. Audit trail"

curl -sS "$BASE_URL/api/audit-events?aggregateType=PAYMENT_INTENT&aggregateId=$payment_intent_id" | pretty_json

section "Demo completed"

echo "PaymentIntent ID : $payment_intent_id"
echo "Settlement ID    : $batch_id"
echo "Provider TX ID   : $PROVIDER_TX_ID"
