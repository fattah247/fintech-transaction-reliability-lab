package id.fatarc.portfolio.fintechlab.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payment_intents",
        indexes = {
                @Index(name = "idx_payment_intent_merchant", columnList = "merchantId"),
                @Index(name = "idx_payment_intent_reference", columnList = "merchantReference")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_intent_idempotency", columnNames = {"idempotencyKey"})
        }
)
public class PaymentIntent {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private String merchantReference;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentState state;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected PaymentIntent() {
    }

    public PaymentIntent(String merchantId, String merchantReference, BigDecimal amount, String currency, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.merchantId = merchantId;
        this.merchantReference = merchantReference;
        this.amount = amount;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
        this.state = PaymentState.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public PaymentState start() {
        return transitionTo(PaymentState.PENDING, "Only a newly created intent can be started.", PaymentState.CREATED);
    }

    public PaymentState authorize() {
        return transitionTo(PaymentState.AUTHORIZED, "Only a pending payment can be authorized.", PaymentState.PENDING);
    }

    public PaymentState succeed() {
        return transitionTo(PaymentState.SUCCESS, "Only a pending or authorized payment can succeed.",
                PaymentState.PENDING, PaymentState.AUTHORIZED);
    }

    public PaymentState fail() {
        return transitionTo(PaymentState.FAILED, "Only a pending or authorized payment can fail.",
                PaymentState.PENDING, PaymentState.AUTHORIZED);
    }

    public PaymentState expire() {
        return transitionTo(PaymentState.EXPIRED, "Only a created, pending, or authorized payment can expire.",
                PaymentState.CREATED, PaymentState.PENDING, PaymentState.AUTHORIZED);
    }

    public PaymentState requireReversal() {
        return transitionTo(PaymentState.REVERSAL_REQUIRED, "Only an authorized or successful payment can require reversal.",
                PaymentState.AUTHORIZED, PaymentState.SUCCESS);
    }

    public PaymentState settle() {
        return transitionTo(PaymentState.SETTLED, "Only a successful payment can be settled.", PaymentState.SUCCESS);
    }

    public PaymentState reconcile() {
        return transitionTo(PaymentState.RECONCILED, "Only a settled payment can be reconciled.", PaymentState.SETTLED);
    }

    public PaymentState requestRefund() {
        return transitionTo(PaymentState.REFUND_REQUESTED,
                "Only successful or settled payments can request a refund.",
                PaymentState.SUCCESS, PaymentState.SETTLED);
    }

    public PaymentState refund() {
        return transitionTo(PaymentState.REFUNDED, "Refund must be requested first.", PaymentState.REFUND_REQUESTED);
    }

    public PaymentState moveToManualReview() {
        if (state == PaymentState.MANUAL_REVIEW) {
            return state;
        }
        PaymentState previousState = state;
        state = PaymentState.MANUAL_REVIEW;
        updatedAt = Instant.now();
        return previousState;
    }

    public boolean matchesRequest(String merchantId, String merchantReference, BigDecimal amount, String currency) {
        return this.merchantId.equals(merchantId)
                && this.merchantReference.equals(merchantReference)
                && this.amount.compareTo(amount) == 0
                && this.currency.equals(currency);
    }

    private PaymentState transitionTo(PaymentState nextState, String message, PaymentState... allowedStates) {
        for (PaymentState allowedState : allowedStates) {
            if (state == allowedState) {
                PaymentState previousState = state;
                state = nextState;
                updatedAt = Instant.now();
                return previousState;
            }
        }
        throw new IllegalStateException(message + " Current state: " + state);
    }

    public UUID getId() { return id; }
    public String getMerchantId() { return merchantId; }
    public String getMerchantReference() { return merchantReference; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public PaymentState getState() { return state; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
