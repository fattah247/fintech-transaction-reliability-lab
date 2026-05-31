package id.fatarc.portfolio.payflowreliability.common;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message
) {
}
