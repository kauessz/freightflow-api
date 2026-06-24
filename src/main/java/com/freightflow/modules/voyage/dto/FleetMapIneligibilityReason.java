package com.freightflow.modules.voyage.dto;

public enum FleetMapIneligibilityReason {
    MISSING_IMO,
    MISSING_ORIGIN_PORT,
    MISSING_DESTINATION_PORT,
    MISSING_SCHEDULE,
    NO_LINKED_SHIPMENTS,
    INACTIVE_VOYAGE
}
