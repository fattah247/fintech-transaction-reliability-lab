package id.fatarc.portfolio.fintechlab.payment;

import id.fatarc.portfolio.fintechlab.payment.dto.PaymentIntentResponse;
import id.fatarc.portfolio.fintechlab.payment.dto.ProviderWebhookRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fake-provider/webhook")
public class ProviderWebhookController {
    private final ProviderWebhookService providerWebhookService;

    public ProviderWebhookController(ProviderWebhookService providerWebhookService) {
        this.providerWebhookService = providerWebhookService;
    }

    @PostMapping
    public PaymentIntentResponse process(
            @RequestHeader("X-Provider-Signature") String signature,
            @Valid @RequestBody ProviderWebhookRequest request
    ) {
        return PaymentIntentResponse.from(providerWebhookService.process(request, signature));
    }
}
