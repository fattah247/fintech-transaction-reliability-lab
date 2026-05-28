package id.fatarc.portfolio.fintechlab.refund;

import id.fatarc.portfolio.fintechlab.refund.dto.CreateRefundRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {
    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/payment-intents/{paymentIntentId}")
    public RefundRequest requestRefund(@PathVariable UUID paymentIntentId,
                                       @Valid @RequestBody CreateRefundRequest request) {
        return refundService.requestRefund(paymentIntentId, request);
    }

    @PostMapping("/{refundId}/complete")
    public RefundRequest completeRefund(@PathVariable UUID refundId) {
        return refundService.completeRefund(refundId);
    }
}
