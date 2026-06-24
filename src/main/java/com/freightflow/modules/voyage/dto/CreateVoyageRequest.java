package com.freightflow.modules.voyage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateVoyageRequest(
    @NotBlank(message = "Voyage number is required")
    @Size(min = 2, max = 30, message = "Voyage number must be between 2 and 30 characters")
    String voyageNumber,

    @NotNull(message = "Vessel ID is required")
    UUID vesselId,

    @NotNull(message = "Origin port ID is required")
    UUID originPortId,

    @NotNull(message = "Destination port ID is required")
    UUID destinationPortId,

    @NotNull(message = "ETD is required")
    Instant etd,

    @NotNull(message = "ETA is required")
    Instant eta,

    Boolean active
) {}
