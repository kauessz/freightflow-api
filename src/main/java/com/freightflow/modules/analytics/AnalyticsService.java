package com.freightflow.modules.analytics;

import com.freightflow.modules.alert.enums.Severity;
import com.freightflow.modules.analytics.dto.DelayStatsResponse;
import com.freightflow.modules.analytics.dto.DelayStatsResponse.RouteDelayStat;
import com.freightflow.modules.analytics.dto.DelayStatsResponse.VesselDelayStat;
import com.freightflow.modules.analytics.dto.OperationsDashboardResponse;
import com.freightflow.modules.analytics.dto.PerformanceResponse;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Analytics service — assembles multi-query aggregations into typed response DTOs.
 *
 * All three methods are cached under "analytics-dashboard" (TTL 2 minutes) with
 * tenant-scoped keys, so cross-tenant cache pollution is impossible.
 *
 * Cache keys:
 *   ops::<tenantId>    → getOperationsDashboard
 *   delays::<tenantId> → getDelayStats
 *   perf::<tenantId>   → getPerformance
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    // ── Operations Dashboard ──────────────────────────────────────────────

    @Cacheable(value = "analytics-dashboard", key = "'ops::' + #tenantId")
    public OperationsDashboardResponse getOperationsDashboard(UUID tenantId) {
        log.debug("Computing operations dashboard for tenant={}", tenantId);

        long totalShipments = analyticsRepository.countActiveShipments(tenantId);
        long inTransit      = analyticsRepository.countInTransit(tenantId);
        long delayed        = analyticsRepository.countDelayed(tenantId);
        long atRisk         = analyticsRepository.countAtRisk(tenantId);
        long awaitingDocs   = analyticsRepository.countAwaitingDocs(tenantId);
        long openAlerts     = analyticsRepository.countOpenAlerts(tenantId);
        long criticalAlerts = analyticsRepository.countOpenAlertsBySeverity(tenantId, Severity.CRITICAL);
        long highAlerts     = analyticsRepository.countOpenAlertsBySeverity(tenantId, Severity.HIGH);

        Map<String, Long> byStatus  = buildStatusMap(analyticsRepository.countByStatus(tenantId));
        Map<String, Long> byCarrier = buildCarrierMap(analyticsRepository.countByCarrierTop5(tenantId));

        return new OperationsDashboardResponse(
                totalShipments, inTransit, delayed, atRisk, awaitingDocs,
                openAlerts, criticalAlerts, highAlerts,
                byStatus, byCarrier
        );
    }

    // ── Delay Stats ───────────────────────────────────────────────────────

    @Cacheable(value = "analytics-dashboard", key = "'delays::' + #tenantId")
    public DelayStatsResponse getDelayStats(UUID tenantId) {
        log.debug("Computing delay stats for tenant={}", tenantId);

        long totalActive  = analyticsRepository.countActiveShipments(tenantId);
        long totalDelayed = analyticsRepository.countDelayed(tenantId);
        double overallDelayRate = roundOneDecimal(
                totalActive > 0 ? (double) totalDelayed / totalActive * 100 : 0.0);

        List<RouteDelayStat>  byRoute  = buildRouteStats(analyticsRepository.delayStatsByRoute(tenantId));
        List<VesselDelayStat> byVessel = buildVesselStats(analyticsRepository.delayStatsByVessel(tenantId));

        return new DelayStatsResponse(overallDelayRate, byRoute, byVessel);
    }

    // ── Performance ───────────────────────────────────────────────────────

    @Cacheable(value = "analytics-dashboard", key = "'perf::' + #tenantId")
    public PerformanceResponse getPerformance(UUID tenantId) {
        log.debug("Computing performance metrics for tenant={}", tenantId);

        long   totalDelivered  = analyticsRepository.countDelivered(tenantId);
        long   totalCancelled  = analyticsRepository.countCancelled(tenantId);
        long   deliveredOnTime = analyticsRepository.countDeliveredOnTime(tenantId);
        double avgDelayDays    = analyticsRepository.avgDelayDaysAll(tenantId);

        double onTimeRate = totalDelivered > 0
                ? roundOneDecimal((double) deliveredOnTime / totalDelivered * 100)
                : 100.0;

        Map<String, Double> onTimeByCarrier =
                buildOnTimeCarrierMap(analyticsRepository.onTimeRateByCarrier(tenantId));

        return new PerformanceResponse(onTimeRate, avgDelayDays, totalDelivered, totalCancelled, onTimeByCarrier);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Converts JPQL GROUP BY status result into a Map<statusName, count>.
     */
    private Map<String, Long> buildStatusMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((ShipmentStatus) row[0]).name(),
                row -> (Long) row[1]
        ));
    }

    /**
     * Converts native top-5 carrier result into an ordered Map<carrier, count>.
     * Uses LinkedHashMap to preserve the ORDER BY cnt DESC ordering.
     */
    private Map<String, Long> buildCarrierMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String carrier = (String) row[0];
            long   count   = ((Number) row[1]).longValue();
            map.put(carrier, count);
        }
        return map;
    }

    /**
     * Maps native route-delay rows to typed {@link RouteDelayStat} records.
     */
    private List<RouteDelayStat> buildRouteStats(List<Object[]> rows) {
        return rows.stream().map(row -> {
            String originUnlocode = (String) row[0];
            String destUnlocode   = (String) row[1];
            long   total          = ((Number) row[2]).longValue();
            long   delayed        = ((Number) row[3]).longValue();
            double avgDelay       = ((Number) row[4]).doubleValue();
            double rate           = total > 0 ? roundOneDecimal((double) delayed / total * 100) : 0.0;
            return new RouteDelayStat(originUnlocode, destUnlocode, total, delayed, avgDelay, rate);
        }).toList();
    }

    /**
     * Maps native vessel-delay rows to typed {@link VesselDelayStat} records.
     */
    private List<VesselDelayStat> buildVesselStats(List<Object[]> rows) {
        return rows.stream().map(row -> {
            String name     = (String) row[0];
            String imo      = (String) row[1];
            long   total    = ((Number) row[2]).longValue();
            long   delayed  = ((Number) row[3]).longValue();
            double avgDelay = ((Number) row[4]).doubleValue();
            double rate     = total > 0 ? roundOneDecimal((double) delayed / total * 100) : 0.0;
            return new VesselDelayStat(name, imo, total, delayed, avgDelay, rate);
        }).toList();
    }

    /**
     * Maps native carrier on-time rows to a Map<carrier, onTimePct>.
     */
    private Map<String, Double> buildOnTimeCarrierMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> roundOneDecimal(((Number) row[1]).doubleValue())
        ));
    }

    /**
     * Rounds a value to one decimal place (e.g. 33.333... → 33.3).
     */
    private double roundOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
