package com.freightflow.modules.operational.capability;

import java.time.Instant;
import java.util.List;

public record OperationalCapabilitySnapshot(
        List<OperationalCapabilityAvailability> capabilities,
        Instant evaluatedAt
) {
}
