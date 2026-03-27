package com.freightflow.modules.voyage.dto;

import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.enums.VoyageStatus;

import java.time.Instant;
import java.util.UUID;

public record VoyageResponse(
    UUID id,
    String voyageNumber,
    VoyageStatus status,
    String vesselName,
    String vesselImo,
    String originPortName,
    String originPortUnlocode,
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
        return new VoyageResponse(
            voyage.getId(),
            voyage.getVoyageNumber(),
            voyage.getStatus(),
            voyage.getVessel().getName(),
            voyage.getVessel().getImo(),
            voyage.getOriginPort().getName(),
            voyage.getOriginPort().getUnlocode(),
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
}
