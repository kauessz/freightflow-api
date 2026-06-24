package com.freightflow.modules.analytics.dto;

import java.util.Map;

/**
 * Aggregated KPI snapshot for the operational dashboard.
 *
 * Returned by GET /api/v1/analytics/operations-dashboard.
 * All counts are scoped to the caller's tenant.
 */
public record OperationsDashboardResponse(

        /** Total active shipments (excludes CANCELLED and DELIVERED). */
        long totalShipments,

        /** Shipments currently in status IN_TRANSIT. */
        long inTransit,

        /** Active shipments with delayDays > 0. */
        long delayed,

        /** Active shipments with riskLevel HIGH or CRITICAL. */
        long atRisk,

        /** Active shipments awaiting documentation (documentStatus=PENDING or customsStatus pending). */
        long awaitingDocs,

        /** Open (unresolved) alerts across all shipments. */
        long openAlerts,

        /** Open alerts with severity CRITICAL. */
        long criticalAlerts,

        /** Open alerts with severity HIGH. */
        long highAlerts,

        /** Shipment count grouped by ShipmentStatus name. */
        Map<String, Long> byStatus,

        /** Shipment count per carrier — top 5, ordered by count descending. */
        Map<String, Long> byCarrier

) {}
