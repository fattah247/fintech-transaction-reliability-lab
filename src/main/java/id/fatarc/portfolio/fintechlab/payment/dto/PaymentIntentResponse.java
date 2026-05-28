package id.fatarc.portfolio.fintechlab.payment.dto;

import id.fatarc.portfolio.fintechlab.payment.PaymentIntent;
import id.fatarc.portfolio.fintechlab.payment.PaymentState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentIntentResponse(
        UUID id,
        String merchantId,
        String merchantReference,
        BigDecimal amount,
        String currency,
        PaymentState state,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentIntentResponse from(PaymentIntent intent) {
        return new PaymentIntentResponse(
                intent.getId(),
                intent.getMerchantId(),
                intent.getMerchantReference(),
                intent.getAmount(),
                intent.getCurrency(),
                intent.getState(),
                intent.getCreatedAt(),
                intent.getUpdatedAt()
        );
    }
}
