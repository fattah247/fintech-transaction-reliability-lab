package id.fatarc.portfolio.fintechlab.reconciliation.dto;

public record ReconciliationReportResponse(
        int checkedRows,
        int mismatchCount
) {
}
