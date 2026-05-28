package id.fatarc.portfolio.fintechlab.audit;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-events")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditEvent> history(@RequestParam String aggregateType, @RequestParam String aggregateId) {
        return auditService.history(aggregateType, aggregateId);
    }
}
