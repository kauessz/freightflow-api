package com.freightflow.modules.voyage.dto;

import com.freightflow.modules.voyage.enums.VoyageStatus;

import java.time.Instant;
import java.util.UUID;

public record UpdateVoyageRequest(
    String voyageNumber,
    UUID vesselId,
    UUID originPortId,
    UUID destinationPortId,
    VoyageStatus status,
    Instant etd,
    Instant eta,
    Instant atd,
    Instant ata,
    Boolean active
) {}
