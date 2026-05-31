package id.fatarc.portfolio.payflowreliability.settlement;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "settlement_batch_items", indexes = {
        @Index(name = "idx_settlement_item_batch", columnList = "batchId"),
        @Index(name = "idx_settlement_item_tx", columnList = "transactionId")
})
public class SettlementBatchItem {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID batchId;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant createdAt;

    protected SettlementBatchItem() {
    }

    public SettlementBatchItem(UUID batchId, UUID transactionId, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.batchId = batchId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getBatchId() { return batchId; }
    public UUID getTransactionId() { return transactionId; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
}
