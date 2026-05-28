package id.fatarc.portfolio.fintechlab.refund;

import id.fatarc.portfolio.fintechlab.audit.AuditService;
import id.fatarc.portfolio.fintechlab.common.BusinessException;
import id.fatarc.portfolio.fintechlab.payment.PaymentIntent;
import id.fatarc.portfolio.fintechlab.payment.PaymentIntentRepository;
import id.fatarc.portfolio.fintechlab.refund.dto.CreateRefundRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RefundService {
    private final PaymentIntentRepository paymentIntentRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final AuditService auditService;

    public RefundService(PaymentIntentRepository paymentIntentRepository,
                         RefundRequestRepository refundRequestRepository,
                         AuditService auditService) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.refundRequestRepository = refundRequestRepository;
        this.auditService = auditService;
    }

    @Transactional
    public RefundRequest requestRefund(UUID paymentIntentId, CreateRefundRequest request) {
        PaymentIntent intent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found: " + paymentIntentId));

        if (request.amount().compareTo(intent.getAmount()) > 0) {
            throw new BusinessException("Refund amount cannot exceed original payment amount.");
        }

        if (intent.getState() == id.fatarc.portfolio.fintechlab.payment.PaymentState.RECONCILED) {
            auditService.record("PAYMENT_INTENT", intent.getId().toString(), "REFUND_BLOCKED",
                    "merchant-api", "Refund requires manual review after reconciliation.", "paymentIntentId=" + paymentIntentId);
            throw new BusinessException("Refund requires manual review after reconciliation.");
        }

        try {
            var previousState = intent.requestRefund();
            auditService.recordStateChange("PAYMENT_INTENT", intent.getId().toString(), previousState.name(),
                    intent.getState().name(), "merchant-api", request.reason(), "paymentIntentId=" + paymentIntentId);
        } catch (IllegalStateException ex) {
            throw new BusinessException(ex.getMessage());
        }

        RefundRequest refund = refundRequestRepository.save(new RefundRequest(paymentIntentId, request.amount(), request.reason()));
        auditService.record("PAYMENT_INTENT", intent.getId().toString(), "REFUND_REQUESTED",
                "merchant-api", "Refund requested.", "refundId=" + refund.getId());
        return refund;
    }

    @Transactional
    public RefundRequest completeRefund(UUID refundId) {
        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund request not found: " + refundId));
        PaymentIntent intent = paymentIntentRepository.findById(refund.getPaymentIntentId())
                .orElseThrow(() -> new BusinessException("Payment intent not found: " + refund.getPaymentIntentId()));

        refund.complete();
        var previousState = intent.refund();
        auditService.recordStateChange("PAYMENT_INTENT", intent.getId().toString(), previousState.name(),
                intent.getState().name(), "operations", "Refund completed for request " + refund.getId(),
                "refundId=" + refund.getId());
        return refund;
    }
}
