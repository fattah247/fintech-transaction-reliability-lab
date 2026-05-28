package id.fatarc.portfolio.fintechlab.payment;

import id.fatarc.portfolio.fintechlab.audit.AuditService;
import id.fatarc.portfolio.fintechlab.common.BusinessException;
import id.fatarc.portfolio.fintechlab.payment.dto.CreatePaymentIntentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentIntentService {
    private final PaymentIntentRepository paymentIntentRepository;
    private final AuditService auditService;

    public PaymentIntentService(PaymentIntentRepository paymentIntentRepository, AuditService auditService) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PaymentIntent create(CreatePaymentIntentRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException("Idempotency-Key header is required.");
        }

        return paymentIntentRepository.findByIdempotencyKey(idempotencyKey)
                .map(intent -> reuseIntent(intent, request, idempotencyKey))
                .orElseGet(() -> {
                    PaymentIntent intent = new PaymentIntent(
                            request.merchantId(),
                            request.merchantReference(),
                            request.amount(),
                            request.currency(),
                            idempotencyKey
                    );
                    PaymentIntent createdIntent = paymentIntentRepository.save(intent);
                    auditService.record(
                            "PAYMENT_INTENT",
                            createdIntent.getId().toString(),
                            "PAYMENT_INTENT_CREATED",
                            "merchant-api",
                            "Payment intent created for merchant reference " + request.merchantReference(),
                            "idempotencyKey=" + idempotencyKey
                    );
                    return createdIntent;
                });
    }

    @Transactional
    public PaymentIntent start(UUID paymentIntentId) {
        PaymentIntent intent = getRequired(paymentIntentId);
        try {
            PaymentState previousState = intent.start();
            auditService.recordStateChange(
                    "PAYMENT_INTENT",
                    intent.getId().toString(),
                    previousState.name(),
                    intent.getState().name(),
                    "merchant-api",
                    "Payment execution started.",
                    "merchantReference=" + intent.getMerchantReference()
            );
        } catch (IllegalStateException ex) {
            throw new BusinessException(ex.getMessage());
        }
        return intent;
    }

    @Transactional(readOnly = true)
    public PaymentIntent getRequired(UUID paymentIntentId) {
        return paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found: " + paymentIntentId));
    }

    private PaymentIntent reuseIntent(PaymentIntent intent,
                                      CreatePaymentIntentRequest request,
                                      String idempotencyKey) {
        if (!intent.matchesRequest(
                request.merchantId(),
                request.merchantReference(),
                request.amount(),
                request.currency()
        )) {
            throw new BusinessException("Idempotency key " + idempotencyKey + " was already used for a different request payload.");
        }
        auditService.record(
                "PAYMENT_INTENT",
                intent.getId().toString(),
                "IDEMPOTENCY_REPLAYED",
                "merchant-api",
                "Existing payment intent returned for a repeated request.",
                "idempotencyKey=" + idempotencyKey
        );
        return intent;
    }
}
