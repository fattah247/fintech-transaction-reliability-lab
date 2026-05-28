package id.fatarc.portfolio.fintechlab.payment;

import id.fatarc.portfolio.fintechlab.common.BusinessException;
import id.fatarc.portfolio.fintechlab.payment.dto.CreatePaymentIntentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PaymentIntentServiceTest {
    @Autowired
    private PaymentIntentService paymentIntentService;

    @Test
    void shouldReturnSamePaymentIntentForSameIdempotencyKey() {
        CreatePaymentIntentRequest createRequest = new CreatePaymentIntentRequest(
                "merchant-test-01",
                "ORDER-1",
                new BigDecimal("50000"),
                "IDR"
        );

        PaymentIntent firstIntent = paymentIntentService.create(createRequest, "same-key-001");
        PaymentIntent secondIntent = paymentIntentService.create(createRequest, "same-key-001");

        assertThat(secondIntent.getId()).isEqualTo(firstIntent.getId());
        assertThat(secondIntent.getState()).isEqualTo(PaymentState.CREATED);
    }

    @Test
    void shouldRejectSameIdempotencyKeyWithDifferentPayload() {
        CreatePaymentIntentRequest firstRequest = new CreatePaymentIntentRequest(
                "merchant-test-01",
                "ORDER-1",
                new BigDecimal("50000"),
                "IDR"
        );
        CreatePaymentIntentRequest secondRequest = new CreatePaymentIntentRequest(
                "merchant-test-01",
                "ORDER-2",
                new BigDecimal("75000"),
                "IDR"
        );

        paymentIntentService.create(firstRequest, "same-key-002");

        assertThatThrownBy(() -> paymentIntentService.create(secondRequest, "same-key-002"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("different request payload");
    }

    @Test
    void shouldRejectMissingIdempotencyKeyForPaymentCreation() {
        CreatePaymentIntentRequest request = new CreatePaymentIntentRequest(
                "merchant-test-01",
                "ORDER-3",
                new BigDecimal("25000"),
                "IDR"
        );

        assertThatThrownBy(() -> paymentIntentService.create(request, " "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Idempotency-Key header is required");
    }
}
