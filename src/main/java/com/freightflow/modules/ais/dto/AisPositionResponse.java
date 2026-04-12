package com.freightflow.modules.ais.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record AisPositionResponse(
    String imo,
    Double latitude,
    Double longitude,
    Double speed,
    Double course,
    String status,
    Instant lastUpdate,
    PositionSource positionSource,
    boolean positionEstimated
) {
    public static AisPositionResponse live(
            String imo,
            Double latitude,
            Double longitude,
            Double speed,
            Double course,
            String status,
            Instant lastUpdate
    ) {
        return new AisPositionResponse(
                imo, latitude, longitude, speed, course, status,
                lastUpdate, PositionSource.LIVE, false
        );
    }

    public AisPositionResponse asCached() {
        return new AisPositionResponse(
                imo, latitude, longitude, speed, course, status,
                lastUpdate, PositionSource.CACHED, false
        );
    }

    public static AisPositionResponse estimated(double lat, double lon) {
        return new AisPositionResponse(
            null, lat, lon, null, null,
            "position_estimated", Instant.now(), PositionSource.ESTIMATED, true
        );
    }

    public static AisPositionResponse unavailable(String imo) {
        return new AisPositionResponse(
                imo, null, null, null, null,
                "position_unavailable", null, PositionSource.UNAVAILABLE, false
        );
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * Mantém compatibilidade com clientes antigos que ainda leem `estimated`.
     */
    @JsonProperty("estimated")
    public boolean estimated() {
        return positionEstimated;
    }

    /**
     * Mantém compatibilidade com clientes antigos que ainda leem `timestamp`.
     */
    @JsonProperty("timestamp")
    public Instant timestamp() {
        return lastUpdate;
    }
}
