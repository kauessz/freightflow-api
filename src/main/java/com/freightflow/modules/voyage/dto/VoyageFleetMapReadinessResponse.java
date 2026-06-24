package com.freightflow.modules.voyage.dto;

import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.enums.VoyageStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VoyageFleetMapReadinessResponse(
        UUID voyageId,
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
        long linkedShipmentCount,
        boolean eligibleForFleetMap,
        List<FleetMapIneligibilityReason> ineligibilityReasons
) {
    public static VoyageFleetMapReadinessResponse from(
            Voyage voyage,
            long linkedShipmentCount,
            boolean eligibleForFleetMap,
            List<FleetMapIneligibilityReason> ineligibilityReasons
    ) {
        String carrier = voyage.getVessel().getCarrier() != null && !voyage.getVessel().getCarrier().isBlank()
                ? voyage.getVessel().getCarrier()
                : deriveCarrier(voyage.getVessel().getName());
        return new VoyageFleetMapReadinessResponse(
                voyage.getId(),
                voyage.getVoyageNumber(),
                voyage.getStatus(),
                voyage.isActive(),
                voyage.getVessel().getId(),
                voyage.getVessel().getName(),
                voyage.getVessel().getImo(),
                carrier,
                voyage.getOriginPort() != null ? voyage.getOriginPort().getId() : null,
                voyage.getOriginPort() != null ? voyage.getOriginPort().getName() : null,
                voyage.getOriginPort() != null ? voyage.getOriginPort().getUnlocode() : null,
                voyage.getDestinationPort() != null ? voyage.getDestinationPort().getId() : null,
                voyage.getDestinationPort() != null ? voyage.getDestinationPort().getName() : null,
                voyage.getDestinationPort() != null ? voyage.getDestinationPort().getUnlocode() : null,
                voyage.getEtd(),
                voyage.getEta(),
                linkedShipmentCount,
                eligibleForFleetMap,
                ineligibilityReasons
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
