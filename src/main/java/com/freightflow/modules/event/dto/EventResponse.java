package com.freightflow.modules.event.dto;

import com.freightflow.modules.event.Event;
import com.freightflow.modules.event.enums.EventType;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
    UUID id,
    UUID shipmentId,
    String booking,
    EventType type,
    String location,
    String description,
    Instant occurredAt,
    Instant reportedAt
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
            event.getId(),
            event.getShipment().getId(),
            event.getShipment().getBooking(),
            event.getType(),
            event.getLocation(),
            event.getDescription(),
            event.getOccurredAt(),
            event.getReportedAt()
        );
    }
}
