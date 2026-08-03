package com.freightflow.modules.operational.capability.dto;

import com.freightflow.modules.operational.capability.OperationalCapabilitySnapshot;

import java.time.Instant;
import java.util.List;

public record OperationalCapabilitiesResponse(
        List<OperationalCapabilityResponse> capabilities,
        Instant evaluatedAt
) {
    public static OperationalCapabilitiesResponse from(OperationalCapabilitySnapshot snapshot) {
        return new OperationalCapabilitiesResponse(
                snapshot.capabilities().stream()
                        .map(OperationalCapabilityResponse::from)
                        .toList(),
                snapshot.evaluatedAt()
        );
    }
}
