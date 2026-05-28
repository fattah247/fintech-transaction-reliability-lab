package id.fatarc.portfolio.fintechlab.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementBatchItemRepository extends JpaRepository<SettlementBatchItem, UUID> {
    List<SettlementBatchItem> findByBatchId(UUID batchId);
}
