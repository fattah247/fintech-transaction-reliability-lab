package id.fatarc.portfolio.fintechlab.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, UUID> {
}
