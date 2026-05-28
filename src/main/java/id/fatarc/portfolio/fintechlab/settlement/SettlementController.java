package id.fatarc.portfolio.fintechlab.settlement;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {
    private final SettlementService settlementService;
    private final SettlementBatchRepository settlementBatchRepository;

    public SettlementController(SettlementService settlementService, SettlementBatchRepository settlementBatchRepository) {
        this.settlementService = settlementService;
        this.settlementBatchRepository = settlementBatchRepository;
    }

    @PostMapping("/batches")
    public SettlementBatch createBatch(@RequestParam String merchantId) {
        return settlementService.createBatch(merchantId);
    }

    @GetMapping("/batches")
    public List<SettlementBatch> listBatches() {
        return settlementBatchRepository.findAll();
    }

    @GetMapping("/batches/{batchId}/items")
    public List<SettlementBatchItem> listBatchItems(@PathVariable UUID batchId) {
        return settlementService.getItems(batchId);
    }
}
