package id.fatarc.portfolio.payflowreliability.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentIntentRequest(
        @NotBlank String merchantId,
        @NotBlank String merchantReference,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String currency
) {
}
