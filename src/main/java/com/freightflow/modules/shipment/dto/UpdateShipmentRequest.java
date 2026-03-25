package com.freightflow.modules.shipment.dto;

import com.freightflow.modules.shipment.enums.ContainerType;
import jakarta.validation.constraints.*;

public record UpdateShipmentRequest(
    @Pattern(regexp = "^[A-Z]{4}\\d{7}$", message = "Container number must be 4 letters followed by 7 digits")
    String containerNumber,

    ContainerType containerType,

    @Size(min = 2, max = 200, message = "Consignee must be between 2 and 200 characters")
    String consignee,

    @Size(min = 2, max = 200, message = "Shipper must be between 2 and 200 characters")
    String shipper
) {}
