package id.fatarc.portfolio.fintechlab.payment;

import id.fatarc.portfolio.fintechlab.audit.AuditService;
import id.fatarc.portfolio.fintechlab.common.BusinessException;
import id.fatarc.portfolio.fintechlab.payment.dto.ProviderWebhookRequest;
import id.fatarc.portfolio.fintechlab.review.ReviewCase;
import id.fatarc.portfolio.fintechlab.review.ReviewCaseRepository;
import id.fatarc.portfolio.fintechlab.review.ReviewReason;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderWebhookService {
    private final String demoSignature;
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ReviewCaseRepository reviewCaseRepository;
    private final AuditService auditService;

    public ProviderWebhookService(
            @Value("${payment-lab.provider.demo-signature}") String providerSignature,
            PaymentIntentRepository paymentIntentRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            ReviewCaseRepository reviewCaseRepository,
            AuditService auditService
    ) {
        this.demoSignature = providerSignature;
        this.paymentIntentRepository = paymentIntentRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.reviewCaseRepository = reviewCaseRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PaymentIntent process(ProviderWebhookRequest webhook, String signature) {
        if (!demoSignature.equals(signature)) {
            reviewCaseRepository.save(new ReviewCase(
                    ReviewReason.INVALID_WEBHOOK_SIGNATURE,
                    "PROVIDER_EVENT",
                    webhook.providerEventId(),
                    "Webhook rejected because provider signature is invalid."
                    ,
                    "fake-provider"
            ));
            auditService.record("FAKE_PROVIDER", webhook.providerEventId(), "WEBHOOK_RECEIVED",
                    "fake-provider", "Webhook rejected because provider signature is invalid.", "status=REJECTED");
            throw new BusinessException("Invalid provider signature.");
        }

        auditService.record("FAKE_PROVIDER", webhook.providerEventId(), "WEBHOOK_RECEIVED",
                "fake-provider", "Webhook accepted for processing.", "status=" + webhook.status());

        PaymentIntent intent = paymentIntentRepository.findById(webhook.paymentIntentId())
                .orElseThrow(() -> {
                    reviewCaseRepository.save(new ReviewCase(
                            ReviewReason.PAYMENT_INTENT_NOT_FOUND,
                            "PROVIDER_EVENT",
                            webhook.providerEventId(),
                            "Webhook points to a payment intent that does not exist locally.",
                            "fake-provider"
                    ));
                    return new BusinessException("Payment intent not found: " + webhook.paymentIntentId());
                });

        if (paymentTransactionRepository.findByProviderEventId(webhook.providerEventId()).isPresent()) {
            reviewCaseRepository.save(new ReviewCase(
                    ReviewReason.DUPLICATE_PROVIDER_EVENT,
                    "PROVIDER_EVENT",
                    webhook.providerEventId(),
                    "Duplicate fake provider event received.",
                    "fake-provider"
            ));
            auditService.record("FAKE_PROVIDER", webhook.providerEventId(), "DUPLICATE_WEBHOOK_IGNORED",
                    "fake-provider", "Duplicate fake provider event ignored.", "providerTransactionId=" + webhook.providerTransactionId());
            return intent;
        }

        if (paymentTransactionRepository.findByProviderTransactionId(webhook.providerTransactionId()).isPresent()) {
            reviewCaseRepository.save(new ReviewCase(
                    ReviewReason.DUPLICATE_PROVIDER_EVENT,
                    "PROVIDER_TRANSACTION",
                    webhook.providerTransactionId(),
                    "Duplicate fake provider transaction received with a new event identifier.",
                    "fake-provider"
            ));
            auditService.record("FAKE_PROVIDER", webhook.providerEventId(), "DUPLICATE_TRANSACTION_IGNORED",
                    "fake-provider", "Duplicate fake provider transaction ignored.", "providerTransactionId=" + webhook.providerTransactionId());
            return intent;
        }

        if (intent.getAmount().compareTo(webhook.amount()) != 0 || !intent.getCurrency().equals(webhook.currency())) {
            PaymentState previousState = intent.moveToManualReview();
            reviewCaseRepository.save(new ReviewCase(
                ReviewReason.AMOUNT_MISMATCH,
                "PAYMENT_INTENT",
                intent.getId().toString(),
                "Webhook amount or currency does not match local payment intent.",
                "fake-provider"
            ));
            auditService.recordStateChange("PAYMENT_INTENT", intent.getId().toString(), previousState.name(),
                    intent.getState().name(), "fake-provider",
                    "Amount or currency mismatch found during webhook processing.",
                    "providerTransactionId=" + webhook.providerTransactionId());
            return intent;
        }

        try {
            applyWebhookStatus(intent, webhook);
        } catch (IllegalStateException ex) {
            reviewCaseRepository.save(new ReviewCase(
                    ReviewReason.INVALID_STATE_TRANSITION,
                    "PAYMENT_INTENT",
                    intent.getId().toString(),
                    ex.getMessage(),
                    "fake-provider"
            ));
            auditService.record("PAYMENT_INTENT", intent.getId().toString(), "WEBHOOK_IGNORED",
                    "fake-provider", ex.getMessage(), "status=" + webhook.status());
            return intent;
        }

        paymentTransactionRepository.save(new PaymentTransaction(
                intent.getId(),
                intent.getMerchantId(),
                webhook.providerEventId(),
                webhook.providerTransactionId(),
                webhook.status(),
                webhook.amount(),
                webhook.currency()
        ));

        auditService.record("PAYMENT_INTENT", intent.getId().toString(), "PROVIDER_WEBHOOK_PROCESSED",
                "fake-provider", "Fake provider webhook processed.", "status=" + webhook.status());

        return intent;
    }

    private void applyWebhookStatus(PaymentIntent intent, ProviderWebhookRequest webhook) {
        PaymentState previousState;
        switch (webhook.status()) {
            case AUTHORIZED -> {
                previousState = intent.authorize();
                auditService.recordStateChange("PAYMENT_INTENT", intent.getId().toString(), previousState.name(),
                        intent.getState().name(), "fake-provider",
                        "Provider authorized the payment.",
                        "providerTransactionId=" + webhook.providerTransactionId());
            }
            case SUCCESS -> {
                previousState = intent.succeed();
                auditService.recordStateChange("PAYMENT_INTENT", intent.getId().toString(), previousState.name(),
                        intent.getState().name(), "fake-provider",
                        "Provider marked the payment as successful.",
                        "providerTransactionId=" + webhook.providerTransactionId());
            }
            case FAILED -> {
                previousState = intent.fail();
                auditService.recordStateChange("PAYMENT_INTENT", intent.getId().toString(), previousState.name(),
                        intent.getState().name(), "fake-provider",
                        "Provider marked the payment as failed.",
                        "providerTransactionId=" + webhook.providerTransactionId());
            }
            case EXPIRED -> {
                previousState = intent.expire();
                auditService.recordStateChange("PAYMENT_INTENT", intent.getId().toString(), previousState.name(),
                        intent.getState().name(), "fake-provider",
                        "Provider marked the payment as expired.",
                        "providerTransactionId=" + webhook.providerTransactionId());
            }
            case REVERSAL_REQUIRED -> {
                previousState = intent.requireReversal();
                auditService.recordStateChange("PAYMENT_INTENT", intent.getId().toString(), previousState.name(),
                        intent.getState().name(), "fake-provider",
                        "Provider requested a reversal.",
                        "providerTransactionId=" + webhook.providerTransactionId());
            }
        }
    }
}
