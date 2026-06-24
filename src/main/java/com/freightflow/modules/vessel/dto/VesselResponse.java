package com.freightflow.modules.vessel.dto;

import com.freightflow.modules.vessel.Vessel;
import com.freightflow.modules.vessel.enums.VesselType;

import java.time.Instant;
import java.util.UUID;

public record VesselResponse(
    UUID id,
    String imo,
    String name,
    String carrier,
    String flag,
    VesselType type,
    Integer capacityTeu,
    boolean active,
    int voyageCount,
    Instant createdAt,
    Instant updatedAt
) {
    public static VesselResponse from(Vessel vessel) {
        String carrier = vessel.getCarrier() != null && !vessel.getCarrier().isBlank()
                ? vessel.getCarrier()
                : deriveCarrier(vessel.getName());
        return new VesselResponse(
            vessel.getId(),
            vessel.getImo(),
            vessel.getName(),
            carrier,
            vessel.getFlag(),
            vessel.getType(),
            vessel.getCapacityTeu(),
            vessel.isActive(),
            vessel.getVoyages().size(),
            vessel.getCreatedAt(),
            vessel.getUpdatedAt()
        );
    }

    private static String deriveCarrier(String vesselName) {
        if (vesselName == null) return "Other";
        String upper = vesselName.toUpperCase();
        if (upper.contains("CMA CGM")) return "CMA CGM";
        if (upper.contains("HMM")) return "HMM";
        if (upper.contains("LOG IN") || upper.contains("LOG-IN")) return "Log-In";
        if (upper.contains("MAERSK") || upper.contains("SAN NICOLAS")) return "Maersk";
        if (upper.contains("MSC")) return "MSC";
        if (upper.startsWith("ONE") || upper.contains(" ONE ")) return "ONE";
        return "Other";
    }
}
