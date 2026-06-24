package com.freightflow.modules.voyage.dto;

import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.enums.VoyageStatus;

import java.time.Instant;
import java.util.UUID;

public record VoyageResponse(
    UUID id,
    String voyageNumber,
    VoyageStatus status,
    boolean active,
    UUID vesselId,
    String vesselName,
    String vesselImo,
    String carrier,
    UUID originPortId,
    String originPortName,
    String originPortUnlocode,
    UUID destinationPortId,
    String destinationPortName,
    String destinationPortUnlocode,
    Instant etd,
    Instant eta,
    Instant atd,
    Instant ata,
    long estimatedTransitTimeHours,
    boolean delayed,
    int shipmentCount,
    Instant createdAt,
    Instant updatedAt
) {
    public static VoyageResponse from(Voyage voyage) {
        String carrier = voyage.getVessel().getCarrier() != null && !voyage.getVessel().getCarrier().isBlank()
                ? voyage.getVessel().getCarrier()
                : deriveCarrier(voyage.getVessel().getName());
        return new VoyageResponse(
            voyage.getId(),
            voyage.getVoyageNumber(),
            voyage.getStatus(),
            voyage.isActive(),
            voyage.getVessel().getId(),
            voyage.getVessel().getName(),
            voyage.getVessel().getImo(),
            carrier,
            voyage.getOriginPort().getId(),
            voyage.getOriginPort().getName(),
            voyage.getOriginPort().getUnlocode(),
            voyage.getDestinationPort().getId(),
            voyage.getDestinationPort().getName(),
            voyage.getDestinationPort().getUnlocode(),
            voyage.getEtd(),
            voyage.getEta(),
            voyage.getAtd(),
            voyage.getAta(),
            voyage.estimatedTransitTimeHours(),
            voyage.isDelayed(),
            voyage.getShipments().size(),
            voyage.getCreatedAt(),
            voyage.getUpdatedAt()
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
