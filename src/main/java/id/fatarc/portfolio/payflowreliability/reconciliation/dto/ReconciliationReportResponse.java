package id.fatarc.portfolio.payflowreliability.reconciliation.dto;

public record ReconciliationReportResponse(
        int checkedRows,
        int mismatchCount
) {
}
