package com.freightflow.modules.voyage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.UUID;

public record CreateVoyageRequest(
    @NotBlank(message = "Voyage number is required")
    @Pattern(regexp = "^[A-Z]{2,5}-\\d{4}-\\d{3,5}$",
             message = "Voyage number must follow pattern: XX-YYYY-NNN (e.g., MSC-2026-001)")
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
    Instant eta
) {}
