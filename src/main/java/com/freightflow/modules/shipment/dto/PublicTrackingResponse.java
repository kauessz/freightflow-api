package com.freightflow.modules.shipment.dto;

import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.shipment.enums.ShipmentStatus;

import java.time.Instant;
import java.util.List;

/**
 * Public-safe tracking contract for anonymous booking lookup.
 *
 * <p>Intentionally excludes commercial, customs, document and internal-risk fields.</p>
 */
public record PublicTrackingResponse(
        String booking,
        String containerNumber,
        ShipmentStatus status,
        String statusMessage,
        String vesselName,
        String voyageNumber,
        String originPort,
        String originPortUnlocode,
        String destinationPort,
        String destinationPortUnlocode,
        Instant etd,
        Instant eta,
        Instant lastUpdate,
        List<PublicTrackingMilestone> milestones
) {
    public record PublicTrackingMilestone(
            EventType type,
            String location,
            Instant occurredAt
    ) {}
}
