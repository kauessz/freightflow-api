package com.freightflow.modules.operational.capability.dto;

import com.freightflow.modules.operational.capability.OperationalCapabilityAvailability;

public record OperationalCapabilityResponse(
        String key,
        boolean available
) {
    public static OperationalCapabilityResponse from(OperationalCapabilityAvailability capability) {
        return new OperationalCapabilityResponse(capability.key(), capability.available());
    }
}
