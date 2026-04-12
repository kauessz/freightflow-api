package com.freightflow.modules.alert.dto;

import com.freightflow.modules.alert.enums.AlertType;
import com.freightflow.modules.alert.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAlertRequest(
        @NotNull(message = "Shipment ID is required")
        UUID shipmentId,

        @NotNull(message = "Alert type is required")
        AlertType type,

        @NotNull(message = "Severity is required")
        Severity severity,

        @NotBlank(message = "Message is required")
        @Size(max = 500, message = "Message must be at most 500 characters")
        String message
) {}
