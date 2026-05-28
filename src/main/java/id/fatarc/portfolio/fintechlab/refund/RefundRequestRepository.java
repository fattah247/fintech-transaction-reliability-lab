package id.fatarc.portfolio.fintechlab.refund;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, UUID> {
}
