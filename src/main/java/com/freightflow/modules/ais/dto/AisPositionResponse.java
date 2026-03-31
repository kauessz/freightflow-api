package com.freightflow.modules.ais.dto;

import java.time.Instant;

public record AisPositionResponse(
    String imo,
    Double latitude,
    Double longitude,
    Double speed,
    Double course,
    String status,
    Instant timestamp,
    boolean estimated
) {
    public static AisPositionResponse estimated(double lat, double lon) {
        return new AisPositionResponse(
            null, lat, lon, null, null,
            "position_estimated", Instant.now(), true
        );
    }
}
