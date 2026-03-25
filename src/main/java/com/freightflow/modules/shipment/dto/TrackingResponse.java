package com.freightflow.modules.shipment.dto;

import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import java.time.Instant;
import java.util.List;

public record TrackingResponse(
    String booking,
    String containerNumber,
    ShipmentStatus status,
    String vesselName,
    String originPort,
    String destinationPort,
    Instant etd,
    Instant eta,
    List<TrackingEvent> events
) {
    public record TrackingEvent(
        EventType type,
        String location,
        Instant occurredAt,
        String description
    ) {}
}
