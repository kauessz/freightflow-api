package com.freightflow.modules.analytics.dto;

import java.util.Map;

/**
 * Delivery performance metrics for the tenant.
 *
 * Returned by GET /api/v1/analytics/performance.
 * All figures are scoped to the caller's tenant.
 */
public record PerformanceResponse(

        /**
         * Percentage of DELIVERED shipments that arrived with delayDays ≤ 0.
         * Rounded to one decimal place. Returns 100.0 when no deliveries exist.
         */
        double onTimeRate,

        /**
         * Average delay days across all non-cancelled shipments.
         * Returns 0.0 when no shipments exist.
         */
        double avgDelayDays,

        /** Total number of shipments in DELIVERED status. */
        long totalDelivered,

        /** Total number of shipments in CANCELLED status. */
        long totalCancelled,

        /**
         * On-time delivery rate per carrier (percentage, 1 decimal place).
         * Only carriers with at least one DELIVERED shipment are included.
         */
        Map<String, Double> onTimeByCarrier

) {}
