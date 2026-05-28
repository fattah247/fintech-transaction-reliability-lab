package id.fatarc.portfolio.fintechlab.review;

import id.fatarc.portfolio.fintechlab.review.dto.ResolveReviewCaseRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/review-cases")
public class ReviewController {
    private final ReviewCaseRepository reviewCaseRepository;
    private final ReviewService reviewService;

    public ReviewController(ReviewCaseRepository reviewCaseRepository, ReviewService reviewService) {
        this.reviewCaseRepository = reviewCaseRepository;
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewCase> listOpenCases() {
        return reviewCaseRepository.findByStatusOrderByCreatedAtAsc(ReviewStatus.OPEN);
    }

    @PostMapping("/{reviewCaseId}/resolve")
    public ReviewCase resolve(@PathVariable UUID reviewCaseId, @Valid @RequestBody ResolveReviewCaseRequest request) {
        return reviewService.resolve(reviewCaseId, request);
    }
}
