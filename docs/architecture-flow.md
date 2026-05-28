flowchart TD
A[Merchant / API Client] --> B[POST /api/payment-intents]
B --> C[PaymentIntentController]
C --> D[PaymentIntentService]

    D --> E{Idempotency-Key valid?}
    E -- No / blank --> E1[Reject request<br/>BusinessException]
    E -- Yes --> F{Existing intent<br/>with same key?}

    F -- Yes --> G{Payload matches<br/>original request?}
    G -- Yes --> H[Return existing PaymentIntent<br/>Record IDEMPOTENCY_REPLAYED audit]
    G -- No --> G1[Reject reused key<br/>different payload]

    F -- No --> I[Create PaymentIntent<br/>state = CREATED]
    I --> J[Record PAYMENT_INTENT_CREATED audit]

    J --> K[POST /api/payment-intents/{id}/start]
    H --> K
    K --> L[PaymentIntent.start]
    L --> M[State: CREATED -> PENDING]
    M --> N[Record TRANSACTION_STATE_CHANGED audit]

    N --> O[POST /fake-provider/webhook]
    O --> P[ProviderWebhookController]
    P --> Q[ProviderWebhookService]

    Q --> R{Valid provider signature?}
    R -- No --> R1[Create ReviewCase<br/>INVALID_WEBHOOK_SIGNATURE]
    R1 --> R2[Record rejected webhook audit]
    R2 --> R3[Reject webhook]

    R -- Yes --> S[Record WEBHOOK_RECEIVED audit]
    S --> T{PaymentIntent exists?}
    T -- No --> T1[Create ReviewCase<br/>PAYMENT_INTENT_NOT_FOUND]
    T1 --> T2[Reject webhook]

    T -- Yes --> U{Duplicate providerEventId?}
    U -- Yes --> U1[Create ReviewCase<br/>DUPLICATE_PROVIDER_EVENT]
    U1 --> U2[Record DUPLICATE_WEBHOOK_IGNORED audit]
    U2 --> U3[Return existing intent]

    U -- No --> V{Duplicate providerTransactionId?}
    V -- Yes --> V1[Create ReviewCase<br/>DUPLICATE_PROVIDER_EVENT]
    V1 --> V2[Record DUPLICATE_TRANSACTION_IGNORED audit]
    V2 --> V3[Return existing intent]

    V -- No --> W{Amount/currency matches<br/>local intent?}
    W -- No --> W1[Move PaymentIntent to MANUAL_REVIEW]
    W1 --> W2[Create ReviewCase<br/>AMOUNT_MISMATCH]
    W2 --> W3[Record state-change audit]
    W3 --> W4[Return intent]

    W -- Yes --> X{Webhook status}
    X --> X1[AUTHORIZED]
    X --> X2[SUCCESS]
    X --> X3[FAILED]
    X --> X4[EXPIRED]
    X --> X5[REVERSAL_REQUIRED]

    X1 --> Y[Apply guarded state transition]
    X2 --> Y
    X3 --> Y
    X4 --> Y
    X5 --> Y

    Y --> Z{Transition valid?}
    Z -- No --> Z1[Create ReviewCase<br/>INVALID_STATE_TRANSITION]
    Z1 --> Z2[Record WEBHOOK_IGNORED audit]
    Z2 --> Z3[Return intent]

    Z -- Yes --> AA[Save PaymentTransaction]
    AA --> AB[Record PROVIDER_WEBHOOK_PROCESSED audit]

    AB --> AC[POST /api/settlements/batches]
    AC --> AD[SettlementService]
    AD --> AE[Find successful unsettled transactions]
    AE --> AF{Any pending<br/>successful tx?}
    AF -- No --> AF1[Reject: no unsettled successful transactions]
    AF -- Yes --> AG{Mixed currency?}
    AG -- Yes --> AG1[Reject: mixed currency not supported]
    AG -- No --> AH[Create SettlementBatch]
    AH --> AI[Create SettlementBatchItem]
    AI --> AJ[Mark PaymentTransaction settled]
    AJ --> AK[PaymentIntent.settle]
    AK --> AL[State: SUCCESS -> SETTLED]
    AL --> AM[Record settlement audit]

    AM --> AN[POST /api/reconciliation/reports]
    AN --> AO[ReconciliationService]
    AO --> AP[Compare provider report rows<br/>against local transactions]

    AP --> AQ{Mismatch found?}
    AQ -- Yes --> AR[Create ReviewCase<br/>amount/currency/status/date/not-found/duplicate]
    AR --> AS[Record RECONCILIATION_MISMATCH_FOUND audit]

    AQ -- No --> AT{Intent is SETTLED?}
    AT -- Yes --> AU[PaymentIntent.reconcile]
    AU --> AV[State: SETTLED -> RECONCILED]
    AV --> AW[Record reconciliation audit]
    AT -- No --> AX[No state change]

    AW --> AY[GET /api/audit-events]
    AS --> AY
    AY --> AZ[Audit trail visible by aggregateType + aggregateId]

    AW --> BA[Optional refund flow]
    BA --> BB[POST /api/refunds/payment-intents/{id}]
    BB --> BC{Payment already RECONCILED?}
    BC -- Yes --> BD[Block refund<br/>manual review required]
    BC -- No --> BE[State: SUCCESS/SETTLED -> REFUND_REQUESTED]
    BE --> BF[POST /api/refunds/{refundId}/complete]
    BF --> BG[State: REFUND_REQUESTED -> REFUNDED]
