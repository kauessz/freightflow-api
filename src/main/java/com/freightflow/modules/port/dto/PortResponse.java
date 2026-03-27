package com.freightflow.modules.port.dto;

import com.freightflow.modules.port.Port;

import java.util.UUID;

public record PortResponse(
    UUID id,
    String unlocode,
    String name,
    String country,
    String timezone,
    Double latitude,
    Double longitude
) {
    public static PortResponse from(Port port) {
        return new PortResponse(
            port.getId(),
            port.getUnlocode(),
            port.getName(),
            port.getCountry(),
            port.getTimezone(),
            port.getLatitude(),
            port.getLongitude()
        );
    }
}
