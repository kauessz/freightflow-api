package com.freightflow.modules.vessel.dto;

import java.time.LocalDateTime;

/**
 * Single AIS position snapshot for a vessel's track history.
 *
 * Used in the response of GET /api/v1/vessels/{imo}/track.
 * Each point corresponds to one POSITION_UPDATE event recorded by PositionTrackingJob.
 *
 * @param lat        latitude (degrees, decimal)
 * @param lon        longitude (degrees, decimal)
 * @param occurredAt UTC timestamp when the position was recorded
 * @param speed      vessel speed in knots at the time of recording, or null if unparseable
 * @param voyageId   ID of the voyage this shipment belongs to
 */
public record PositionTrackPoint(
        Double lat,
        Double lon,
        LocalDateTime occurredAt,
        Double speed,
        String voyageId
) {}
