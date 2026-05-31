package id.fatarc.portfolio.payflowreliability.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {
    public static final String SYSTEM_ACTOR = "system";

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void record(String aggregateType, String aggregateId, String eventType, String actor, String reason, String metadata) {
        auditEventRepository.save(new AuditEvent(
                aggregateType,
                aggregateId,
                eventType,
                null,
                null,
                actor,
                reason,
                metadata
        ));
    }

    @Transactional
    public void recordStateChange(String aggregateType,
                                  String aggregateId,
                                  String previousState,
                                  String newState,
                                  String actor,
                                  String reason,
                                  String metadata) {
        auditEventRepository.save(new AuditEvent(
                aggregateType,
                aggregateId,
                "TRANSACTION_STATE_CHANGED",
                previousState,
                newState,
                actor,
                reason,
                metadata
        ));
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> history(String aggregateType, String aggregateId) {
        return auditEventRepository.findByAggregateTypeAndAggregateIdOrderByCreatedAtAsc(aggregateType, aggregateId);
    }
}
