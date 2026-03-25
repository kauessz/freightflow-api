package com.freightflow.modules.shipment.dto;

import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.shipment.enums.ContainerType;
import java.util.UUID;
import java.time.Instant;

public record ShipmentResponse(
    UUID id,
    String booking,
    String containerNumber,
    ContainerType containerType,
    ShipmentStatus status,
    String originPortName,
    String originPortUnlocode,
    String destinationPortName,
    String destinationPortUnlocode,
    String vesselName,
    String voyageNumber,
    Instant createdAt,
    Instant updatedAt
) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
            shipment.getId(),
            shipment.getBooking(),
            shipment.getContainerNumber(),
            shipment.getContainerType(),
            shipment.getStatus(),
            shipment.getOriginPort().getName(),
            shipment.getOriginPort().getUnlocode(),
            shipment.getDestinationPort().getName(),
            shipment.getDestinationPort().getUnlocode(),
            shipment.getVoyage().getVessel().getName(),
            shipment.getVoyage().getVoyageNumber(),
            shipment.getCreatedAt(),
            shipment.getUpdatedAt()
        );
    }
}
