package id.fatarc.portfolio.payflowreliability.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewCaseRepository extends JpaRepository<ReviewCase, UUID> {
    List<ReviewCase> findByStatusOrderByCreatedAtAsc(ReviewStatus status);
}
