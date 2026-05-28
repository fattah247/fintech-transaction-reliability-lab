package id.fatarc.portfolio.fintechlab.audit;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_aggregate", columnList = "aggregateType,aggregateId"),
        @Index(name = "idx_audit_created", columnList = "createdAt")
})
public class AuditEvent {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    private String previousState;

    private String newState;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false, length = 2000)
    private String metadata;

    @Column(nullable = false)
    private Instant createdAt;

    protected AuditEvent() {
    }

    public AuditEvent(String aggregateType,
                      String aggregateId,
                      String eventType,
                      String previousState,
                      String newState,
                      String actor,
                      String reason,
                      String metadata) {
        this.id = UUID.randomUUID();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.previousState = previousState;
        this.newState = newState;
        this.actor = actor;
        this.reason = reason;
        this.metadata = metadata;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPreviousState() { return previousState; }
    public String getNewState() { return newState; }
    public String getActor() { return actor; }
    public String getReason() { return reason; }
    public String getMetadata() { return metadata; }
    public Instant getCreatedAt() { return createdAt; }
}
