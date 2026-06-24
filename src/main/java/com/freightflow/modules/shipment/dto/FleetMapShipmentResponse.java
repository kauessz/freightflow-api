package com.freightflow.modules.shipment.dto;

import com.freightflow.modules.shipment.Shipment;

import java.time.Instant;
import java.util.UUID;

/**
 * Minimal authenticated shipment shape embedded in Fleet Map responses.
 *
 * <p>Intentionally contains only map-relevant operational fields so the portal
 * can render drawer overlays without composing data from additional endpoints.</p>
 */
public record FleetMapShipmentResponse(
        UUID id,
        String booking,
        String containerNumber,
        String status,
        String riskLevel,
        String vesselName,
        String voyageNumber,
        String carrier,
        String originPortName,
        String originPortUnlocode,
        String destinationPortName,
        String destinationPortUnlocode,
        Instant eta
) {
    public static FleetMapShipmentResponse from(Shipment shipment) {
        String vesselName = shipment.getVoyage().getVessel().getName();
        String explicitCarrier = shipment.getVoyage().getVessel().getCarrier();
        String carrier = (explicitCarrier != null && !explicitCarrier.isBlank())
                ? explicitCarrier
                : deriveCarrier(vesselName);

        return new FleetMapShipmentResponse(
                shipment.getId(),
                shipment.getBooking(),
                shipment.getContainerNumber(),
                shipment.getStatus().name(),
                shipment.getRiskLevel(),
                vesselName,
                shipment.getVoyage().getVoyageNumber(),
                carrier,
                shipment.getOriginPort().getName(),
                shipment.getOriginPort().getUnlocode(),
                shipment.getDestinationPort().getName(),
                shipment.getDestinationPort().getUnlocode(),
                shipment.getVoyage().getEta()
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
