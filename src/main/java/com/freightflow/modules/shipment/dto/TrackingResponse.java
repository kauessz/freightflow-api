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
    String voyageNumber,
    String originPort,
    String originPortUnlocode,
    String destinationPort,
    String destinationPortUnlocode,
    Instant etd,
    Instant eta,
    // Campos operacionais visíveis ao cliente
    String houseBl,
    String masterBl,
    String incoterm,
    String cargoDescription,
    String documentStatus,
    String customsStatus,
    String riskLevel,
    Integer delayDays,
    List<TrackingEvent> events
) {
    public record TrackingEvent(
        EventType type,
        String location,
        Instant occurredAt,
        Instant reportedAt,
        String description
    ) {}
}
