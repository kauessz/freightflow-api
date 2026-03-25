package com.freightflow.modules.shipment.dto;

import com.freightflow.modules.shipment.enums.ContainerType;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record CreateShipmentRequest(
    @NotBlank(message = "Booking is required")
    @Pattern(regexp = "^[A-Z]\\d{8,10}$", message = "Booking must start with letter followed by 8-10 digits")
    String booking,

    @Pattern(regexp = "^[A-Z]{4}\\d{7}$", message = "Container number must be 4 letters followed by 7 digits")
    String containerNumber,

    ContainerType containerType,

    @NotNull(message = "Voyage ID is required")
    UUID voyageId,

    @NotNull(message = "Origin port ID is required")
    UUID originPortId,

    @NotNull(message = "Destination port ID is required")
    UUID destinationPortId,

    @Size(min = 2, max = 200, message = "Consignee must be between 2 and 200 characters")
    String consignee,

    @Size(min = 2, max = 200, message = "Shipper must be between 2 and 200 characters")
    String shipper
) {}
