package com.freightflow.modules.voyage.dto;

import com.freightflow.modules.voyage.enums.VoyageStatus;

import java.time.Instant;

public record UpdateVoyageRequest(
    VoyageStatus status,
    Instant etd,
    Instant eta,
    Instant atd,
    Instant ata
) {}
