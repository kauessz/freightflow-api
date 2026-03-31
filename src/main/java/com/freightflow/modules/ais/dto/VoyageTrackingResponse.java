package com.freightflow.modules.ais.dto;

import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.enums.VoyageStatus;

import java.time.Instant;
import java.util.UUID;

public record VoyageTrackingResponse(
    UUID voyageId,
    String voyageNumber,
    VoyageStatus status,
    String vesselName,
    String vesselImo,
    String originPortName,
    String originPortUnlocode,
    Double originLat,
    Double originLon,
    String destinationPortName,
    String destinationPortUnlocode,
    Double destinationLat,
    Double destinationLon,
    Instant etd,
    Instant eta,
    AisPositionResponse vesselPosition
) {
    public static VoyageTrackingResponse from(Voyage voyage, AisPositionResponse position) {
        return new VoyageTrackingResponse(
            voyage.getId(),
            voyage.getVoyageNumber(),
            voyage.getStatus(),
            voyage.getVessel().getName(),
            voyage.getVessel().getImo(),
            voyage.getOriginPort().getName(),
            voyage.getOriginPort().getUnlocode(),
            voyage.getOriginPort().getLatitude(),
            voyage.getOriginPort().getLongitude(),
            voyage.getDestinationPort().getName(),
            voyage.getDestinationPort().getUnlocode(),
            voyage.getDestinationPort().getLatitude(),
            voyage.getDestinationPort().getLongitude(),
            voyage.getEtd(),
            voyage.getEta(),
            position
        );
    }
}
