#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
SIGNATURE="${SIGNATURE:-local-demo-signature}"
KEY="demo-key-001"

need() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "missing dependency: $1" >&2
    exit 1
  }
}

json_field() {
  local field="$1"
  python3 -c 'import json,sys; print(json.load(sys.stdin)[sys.argv[1]])' "$field"
}

need curl
need python3

echo "creating payment intent"
create_response=$(curl -sS -X POST "$BASE_URL/api/payment-intents" \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $KEY" \
  -d '{
    "merchantId": "merchant-demo-01",
    "merchantReference": "ORDER-DEMO-001",
    "amount": 125000,
    "currency": "IDR"
  }')

payment_intent_id=$(printf '%s' "$create_response" | json_field id)
echo "payment_intent_id=$payment_intent_id"

echo "replaying same payment intent request"
replay_response=$(curl -sS -X POST "$BASE_URL/api/payment-intents" \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $KEY" \
  -d '{
    "merchantId": "merchant-demo-01",
    "merchantReference": "ORDER-DEMO-001",
    "amount": 125000,
    "currency": "IDR"
  }')
replay_id=$(printf '%s' "$replay_response" | json_field id)
echo "replay_id=$replay_id"

echo "starting payment"
curl -sS -X POST "$BASE_URL/api/payment-intents/$payment_intent_id/start" >/dev/null

echo "sending success webhook"
curl -sS -X POST "$BASE_URL/fake-provider/webhook" \
  -H 'Content-Type: application/json' \
  -H "X-Provider-Signature: $SIGNATURE" \
  -d "{
    \"providerEventId\": \"evt-demo-001\",
    \"providerTransactionId\": \"provider-tx-demo-001\",
    \"paymentIntentId\": \"$payment_intent_id\",
    \"status\": \"SUCCESS\",
    \"amount\": 125000,
    \"currency\": \"IDR\"
  }" >/dev/null

echo "creating settlement batch"
batch_response=$(curl -sS -X POST "$BASE_URL/api/settlements/batches?merchantId=merchant-demo-01")
batch_id=$(printf '%s' "$batch_response" | json_field id)
echo "batch_id=$batch_id"

echo "sending reconciliation mismatch"
curl -sS -X POST "$BASE_URL/api/reconciliation/reports" \
  -H 'Content-Type: application/json' \
  -d '{
    "rows": [
      {
        "providerTransactionId": "provider-tx-demo-001",
        "amount": 124000,
        "currency": "IDR",
        "providerStatus": "SUCCESS",
        "settlementDate": "2026-01-01"
      }
    ]
  }' >/dev/null

echo "open review cases"
curl -sS "$BASE_URL/api/review-cases"
echo

echo "audit trail"
curl -sS "$BASE_URL/api/audit-events?aggregateType=PAYMENT_INTENT&aggregateId=$payment_intent_id"
echo
