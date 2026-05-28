package id.fatarc.portfolio.fintechlab.reconciliation;

import id.fatarc.portfolio.fintechlab.reconciliation.dto.ReconciliationReportRequest;
import id.fatarc.portfolio.fintechlab.reconciliation.dto.ReconciliationReportResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {
    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/reports")
    public ReconciliationReportResponse importReport(@Valid @RequestBody ReconciliationReportRequest request) {
        return reconciliationService.importReport(request);
    }
}
