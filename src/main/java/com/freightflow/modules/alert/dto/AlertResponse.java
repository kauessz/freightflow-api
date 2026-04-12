package com.freightflow.modules.alert.dto;

import com.freightflow.modules.alert.Alert;
import com.freightflow.modules.alert.enums.AlertType;
import com.freightflow.modules.alert.enums.Severity;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        UUID shipmentId,
        String booking,
        AlertType type,
        Severity severity,
        String message,
        boolean resolved,
        Instant resolvedAt,
        Instant createdAt
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getShipment().getId(),
                alert.getShipment().getBooking(),
                alert.getType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.isResolved(),
                alert.getResolvedAt(),
                alert.getCreatedAt()
        );
    }
}
