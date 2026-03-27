package com.freightflow.modules.vessel.dto;

import com.freightflow.modules.vessel.Vessel;
import com.freightflow.modules.vessel.enums.VesselType;

import java.time.Instant;
import java.util.UUID;

public record VesselResponse(
    UUID id,
    String imo,
    String name,
    String flag,
    VesselType type,
    Integer capacityTeu,
    int voyageCount,
    Instant createdAt,
    Instant updatedAt
) {
    public static VesselResponse from(Vessel vessel) {
        return new VesselResponse(
            vessel.getId(),
            vessel.getImo(),
            vessel.getName(),
            vessel.getFlag(),
            vessel.getType(),
            vessel.getCapacityTeu(),
            vessel.getVoyages().size(),
            vessel.getCreatedAt(),
            vessel.getUpdatedAt()
        );
    }
}
