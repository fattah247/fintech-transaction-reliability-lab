package id.fatarc.portfolio.payflowreliability.payment;

import id.fatarc.portfolio.payflowreliability.payment.dto.CreatePaymentIntentRequest;
import id.fatarc.portfolio.payflowreliability.payment.dto.PaymentIntentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment-intents")
public class PaymentIntentController {
    private final PaymentIntentService paymentIntentService;

    public PaymentIntentController(PaymentIntentService paymentIntentService) {
        this.paymentIntentService = paymentIntentService;
    }

    @PostMapping
    public PaymentIntentResponse create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePaymentIntentRequest request
    ) {
        return PaymentIntentResponse.from(paymentIntentService.create(request, idempotencyKey));
    }

    @PostMapping("/{paymentIntentId}/start")
    public PaymentIntentResponse start(@PathVariable UUID paymentIntentId) {
        return PaymentIntentResponse.from(paymentIntentService.start(paymentIntentId));
    }

    @GetMapping("/{paymentIntentId}")
    public PaymentIntentResponse get(@PathVariable UUID paymentIntentId) {
        return PaymentIntentResponse.from(paymentIntentService.getRequired(paymentIntentId));
    }
}
