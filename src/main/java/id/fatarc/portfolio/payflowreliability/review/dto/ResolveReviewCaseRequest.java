package id.fatarc.portfolio.payflowreliability.review.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveReviewCaseRequest(
        @NotBlank String actor,
        @NotBlank String note
) {
}
