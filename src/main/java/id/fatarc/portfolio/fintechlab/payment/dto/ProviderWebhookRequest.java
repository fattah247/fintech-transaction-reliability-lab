package id.fatarc.portfolio.fintechlab.payment.dto;

import id.fatarc.portfolio.fintechlab.payment.ProviderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProviderWebhookRequest(
        @NotBlank String providerEventId,
        @NotBlank String providerTransactionId,
        @NotNull UUID paymentIntentId,
        @NotNull ProviderStatus status,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String currency
) {
}
