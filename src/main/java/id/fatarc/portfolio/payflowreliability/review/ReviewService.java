package id.fatarc.portfolio.payflowreliability.review;

import id.fatarc.portfolio.payflowreliability.audit.AuditService;
import id.fatarc.portfolio.payflowreliability.common.BusinessException;
import id.fatarc.portfolio.payflowreliability.review.dto.ResolveReviewCaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final ReviewCaseRepository reviewCaseRepository;
    private final AuditService auditService;

    public ReviewService(ReviewCaseRepository reviewCaseRepository, AuditService auditService) {
        this.reviewCaseRepository = reviewCaseRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ReviewCase resolve(java.util.UUID reviewCaseId, ResolveReviewCaseRequest request) {
        ReviewCase reviewCase = reviewCaseRepository.findById(reviewCaseId)
                .orElseThrow(() -> new BusinessException("Review case not found: " + reviewCaseId));

        reviewCase.resolve(request.actor(), request.note());

        auditService.record(
                "MANUAL_REVIEW",
                reviewCase.getId().toString(),
                "MANUAL_REVIEW_RESOLVED",
                request.actor(),
                request.note(),
                "referenceType=" + reviewCase.getReferenceType() + ",referenceId=" + reviewCase.getReferenceId()
        );

        return reviewCaseRepository.save(reviewCase);
    }
}
