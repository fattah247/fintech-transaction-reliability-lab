package id.fatarc.portfolio.payflowreliability.payment;

public enum PaymentState {
    CREATED,
    PENDING,
    AUTHORIZED,
    SUCCESS,
    FAILED,
    EXPIRED,
    SETTLED,
    RECONCILED,
    REFUND_REQUESTED,
    REFUNDED,
    REVERSAL_REQUIRED,
    MANUAL_REVIEW
}
