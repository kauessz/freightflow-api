package com.freightflow.modules.analytics.dto;

import java.util.List;

/**
 * Delay analysis breakdown — overall rate and per-route / per-vessel rankings.
 *
 * Returned by GET /api/v1/analytics/delays.
 * All figures are scoped to the caller's tenant.
 */
public record DelayStatsResponse(

        /**
         * Percentage of active shipments that have at least one delay day.
         * Rounded to one decimal place (e.g. 33.3).
         */
        double overallDelayRate,

        /** Top-5 routes ordered by delayed shipments descending. */
        List<RouteDelayStat> byRoute,

        /** Top-5 vessels ordered by delayed shipments descending. */
        List<VesselDelayStat> byVessel

) {

    /**
     * Aggregated delay metrics for a single origin → destination route.
     *
     * @param originUnlocode  5-character UN/LOCODE of the origin port
     * @param destUnlocode    5-character UN/LOCODE of the destination port
     * @param totalShipments  total shipments on this route for the tenant
     * @param delayedShipments shipments with delayDays > 0
     * @param avgDelayDays    average delay days across all shipments on this route
     * @param delayRate       delayedShipments / totalShipments × 100, rounded to 1 decimal
     */
    public record RouteDelayStat(
            String originUnlocode,
            String destUnlocode,
            long   totalShipments,
            long   delayedShipments,
            double avgDelayDays,
            double delayRate
    ) {}

    /**
     * Aggregated delay metrics for a single vessel.
     *
     * @param vesselName      vessel commercial name
     * @param imo             vessel IMO number
     * @param totalShipments  total shipments transported by this vessel for the tenant
     * @param delayedShipments shipments with delayDays > 0
     * @param avgDelayDays    average delay days
     * @param delayRate       delayedShipments / totalShipments × 100, rounded to 1 decimal
     */
    public record VesselDelayStat(
            String vesselName,
            String imo,
            long   totalShipments,
            long   delayedShipments,
            double avgDelayDays,
            double delayRate
    ) {}
}
