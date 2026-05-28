package id.fatarc.portfolio.fintechlab.reconciliation;

import id.fatarc.portfolio.fintechlab.audit.AuditService;
import id.fatarc.portfolio.fintechlab.payment.PaymentIntent;
import id.fatarc.portfolio.fintechlab.payment.PaymentIntentRepository;
import id.fatarc.portfolio.fintechlab.payment.PaymentState;
import id.fatarc.portfolio.fintechlab.payment.PaymentTransaction;
import id.fatarc.portfolio.fintechlab.payment.PaymentTransactionRepository;
import id.fatarc.portfolio.fintechlab.reconciliation.dto.ReconciliationReportRequest;
import id.fatarc.portfolio.fintechlab.reconciliation.dto.ReconciliationReportResponse;
import id.fatarc.portfolio.fintechlab.review.ReviewCase;
import id.fatarc.portfolio.fintechlab.review.ReviewCaseRepository;
import id.fatarc.portfolio.fintechlab.review.ReviewReason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class ReconciliationService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final ReviewCaseRepository reviewCaseRepository;
    private final AuditService auditService;

    public ReconciliationService(PaymentTransactionRepository paymentTransactionRepository,
                                 PaymentIntentRepository paymentIntentRepository,
                                 ReviewCaseRepository reviewCaseRepository,
                                 AuditService auditService) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.reviewCaseRepository = reviewCaseRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ReconciliationReportResponse importReport(ReconciliationReportRequest report) {
        int mismatchCount = 0;
        Set<String> seenReferences = new HashSet<>();

        for (ReconciliationReportRequest.Row row : report.rows()) {
            int rowMismatchCount = 0;

            if (!seenReferences.add(row.providerTransactionId())) {
                mismatchCount++;
                createReview(ReviewReason.DUPLICATE_PROVIDER_REFERENCE, "PROVIDER_TRANSACTION", row.providerTransactionId(),
                        "Provider report contains the same provider reference more than once.");
                continue;
            }

            PaymentTransaction transaction = paymentTransactionRepository.findByProviderTransactionId(row.providerTransactionId())
                    .orElse(null);

            if (transaction == null) {
                mismatchCount++;
                createReview(ReviewReason.PROVIDER_TRANSACTION_NOT_FOUND, "PROVIDER_TRANSACTION", row.providerTransactionId(),
                        "Provider report contains a transaction not found locally.");
                continue;
            }

            if (transaction.getAmount().compareTo(row.amount()) != 0) {
                mismatchCount++;
                rowMismatchCount++;
                createReview(ReviewReason.AMOUNT_MISMATCH, "PROVIDER_TRANSACTION", row.providerTransactionId(),
                        "Provider amount does not match local transaction amount.");
            }

            if (!transaction.getCurrency().equals(row.currency())) {
                mismatchCount++;
                rowMismatchCount++;
                createReview(ReviewReason.CURRENCY_MISMATCH, "PROVIDER_TRANSACTION", row.providerTransactionId(),
                        "Provider currency does not match local transaction currency.");
            }

            if (!transaction.getProviderStatus().name().equals(row.providerStatus())) {
                mismatchCount++;
                rowMismatchCount++;
                createReview(ReviewReason.STATUS_MISMATCH, "PROVIDER_TRANSACTION", row.providerTransactionId(),
                        "Provider status does not match local transaction status.");
            }

            if (row.settlementDate() != null && !row.settlementDate().equals(transaction.getSettledOn())) {
                mismatchCount++;
                rowMismatchCount++;
                createReview(ReviewReason.SETTLEMENT_DATE_MISMATCH, "PROVIDER_TRANSACTION", row.providerTransactionId(),
                        "Provider settlement date does not match local settlement date.");
            }

            PaymentIntent intent = paymentIntentRepository.findById(transaction.getPaymentIntentId()).orElse(null);
            if (intent != null && (intent.getState() == PaymentState.REFUND_REQUESTED || intent.getState() == PaymentState.REFUNDED)
                    && "SUCCESS".equals(row.providerStatus())) {
                mismatchCount++;
                rowMismatchCount++;
                createReview(ReviewReason.STATUS_MISMATCH, "PAYMENT_INTENT", intent.getId().toString(),
                        "Refunded payment still appears successful in the provider report.");
            }

            if (rowMismatchCount == 0 && intent != null && intent.getState() == PaymentState.SETTLED) {
                PaymentState previousState = intent.reconcile();
                auditService.recordStateChange("PAYMENT_INTENT", intent.getId().toString(), previousState.name(),
                        intent.getState().name(), "reconciliation-job",
                        "Settlement row matched the local transaction.",
                        "providerTransactionId=" + row.providerTransactionId());
            }
        }

        return new ReconciliationReportResponse(report.rows().size(), mismatchCount);
    }

    private void createReview(ReviewReason reason, String referenceType, String referenceId, String details) {
        reviewCaseRepository.save(new ReviewCase(reason, referenceType, referenceId, details, "reconciliation-job"));
        auditService.record(referenceType, referenceId, "RECONCILIATION_MISMATCH_FOUND",
                "reconciliation-job", details, "reason=" + reason);
    }
}
