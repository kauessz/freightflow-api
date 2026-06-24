package com.freightflow.modules.voyage.dto;

import java.time.LocalDateTime;

/**
 * Dynamic ETA calculated from the vessel's current AIS position.
 *
 * Returned by GET /api/v1/voyages/{id}/eta.
 * All date/times are in UTC.
 */
public record RevisedEtaResponse(

        /** Voyage UUID as string. */
        String voyageId,

        /** Voyage number (e.g. MSC-2026-001). */
        String voyageNumber,

        /** Original planned ETA from the voyage record (UTC). */
        LocalDateTime originalEta,

        /** ETA recalculated from current AIS position and speed (UTC). */
        LocalDateTime revisedEta,

        /** Remaining distance to destination port in nautical miles. */
        double distanceNm,

        /** Speed used for the calculation (kn). Either AIS reported or default 14.0 kn. */
        double speedKnots,

        /**
         * Difference in hours between revisedEta and originalEta.
         * Negative value means the vessel is running ahead of schedule.
         */
        long delayHours,

        /**
         * Delay in full days, rounded up (Math.ceil).
         * Always 0 or positive — a negative delay means the vessel is early and this returns 0.
         */
        int delayDays,

        /** AIS source used for the calculation: LIVE, CACHED, ESTIMATED, etc. */
        String positionSource,

        /** Vessel's current latitude from AIS. */
        double currentLat,

        /** Vessel's current longitude from AIS. */
        double currentLon

) {}
