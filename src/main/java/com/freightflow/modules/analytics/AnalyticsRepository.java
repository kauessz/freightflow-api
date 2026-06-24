package com.freightflow.modules.analytics;

import com.freightflow.modules.alert.enums.Severity;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Custom analytics repository built on top of {@link EntityManager}.
 *
 * Uses JPQL for queries that can navigate the object graph,
 * and native PostgreSQL SQL for aggregations that require LIMIT, CASE WHEN,
 * or complex GROUP BY clauses.
 *
 * Native queries pass the tenant UUID as a String and rely on PostgreSQL's
 * implicit VARCHAR → UUID cast when comparing against uuid columns.
 */
@Repository
public class AnalyticsRepository {

    private final EntityManager em;

    public AnalyticsRepository(EntityManager em) {
        this.em = em;
    }

    // ── Dashboard scalar counts ───────────────────────────────────────────

    /**
     * Total active shipments (excludes CANCELLED and DELIVERED).
     */
    public long countActiveShipments(UUID tenantId) {
        return (Long) em.createQuery(
                "SELECT COUNT(s) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId " +
                "  AND s.status NOT IN :finished")
                .setParameter("tenantId", tenantId)
                .setParameter("finished", List.of(ShipmentStatus.CANCELLED, ShipmentStatus.DELIVERED))
                .getSingleResult();
    }

    /**
     * Shipments currently in status IN_TRANSIT.
     */
    public long countInTransit(UUID tenantId) {
        return (Long) em.createQuery(
                "SELECT COUNT(s) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId AND s.status = :status")
                .setParameter("tenantId", tenantId)
                .setParameter("status", ShipmentStatus.IN_TRANSIT)
                .getSingleResult();
    }

    /**
     * Active shipments with delayDays > 0.
     */
    public long countDelayed(UUID tenantId) {
        return (Long) em.createQuery(
                "SELECT COUNT(s) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId " +
                "  AND s.status NOT IN :finished " +
                "  AND s.delayDays > 0")
                .setParameter("tenantId", tenantId)
                .setParameter("finished", List.of(ShipmentStatus.CANCELLED, ShipmentStatus.DELIVERED))
                .getSingleResult();
    }

    /**
     * Active shipments with riskLevel HIGH or CRITICAL.
     */
    public long countAtRisk(UUID tenantId) {
        return (Long) em.createQuery(
                "SELECT COUNT(s) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId " +
                "  AND s.riskLevel IN ('HIGH', 'CRITICAL')")
                .setParameter("tenantId", tenantId)
                .getSingleResult();
    }

    /**
     * Active shipments awaiting documentation:
     * documentStatus = 'PENDING' OR customsStatus IN ('NOT_STARTED', 'PENDING').
     */
    public long countAwaitingDocs(UUID tenantId) {
        return (Long) em.createQuery(
                "SELECT COUNT(s) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId " +
                "  AND s.status NOT IN :finished " +
                "  AND (s.documentStatus = 'PENDING' " +
                "       OR s.customsStatus IN ('NOT_STARTED', 'PENDING'))")
                .setParameter("tenantId", tenantId)
                .setParameter("finished", List.of(ShipmentStatus.CANCELLED, ShipmentStatus.DELIVERED))
                .getSingleResult();
    }

    /**
     * All open (unresolved) alerts across all shipments of this tenant.
     */
    public long countOpenAlerts(UUID tenantId) {
        return (Long) em.createQuery(
                "SELECT COUNT(a) FROM Alert a JOIN a.shipment s " +
                "WHERE s.tenant.id = :tenantId AND a.resolved = false")
                .setParameter("tenantId", tenantId)
                .getSingleResult();
    }

    /**
     * Open alerts filtered by the given severity.
     */
    public long countOpenAlertsBySeverity(UUID tenantId, Severity severity) {
        return (Long) em.createQuery(
                "SELECT COUNT(a) FROM Alert a JOIN a.shipment s " +
                "WHERE s.tenant.id = :tenantId " +
                "  AND a.resolved = false " +
                "  AND a.severity = :severity")
                .setParameter("tenantId", tenantId)
                .setParameter("severity", severity)
                .getSingleResult();
    }

    // ── Dashboard map queries ─────────────────────────────────────────────

    /**
     * Shipment count grouped by status.
     *
     * Returns {@code List<Object[]>} where {@code row[0]} is {@link ShipmentStatus}
     * and {@code row[1]} is {@code Long}.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> countByStatus(UUID tenantId) {
        return em.createQuery(
                "SELECT s.status, COUNT(s) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId " +
                "GROUP BY s.status")
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    /**
     * Top-5 carriers by shipment count, ordered descending.
     *
     * Returns {@code List<Object[]>} where {@code row[0]} is carrier name (String)
     * and {@code row[1]} is count (Number).
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> countByCarrierTop5(UUID tenantId) {
        return em.createNativeQuery(
                "SELECT v.carrier, COUNT(s.id) AS cnt " +
                "FROM shipments s " +
                "JOIN voyages vo ON s.voyage_id = vo.id " +
                "JOIN vessels v  ON vo.vessel_id = v.id " +
                "WHERE s.tenant_id = :tenantId " +
                "  AND v.carrier IS NOT NULL " +
                "GROUP BY v.carrier " +
                "ORDER BY cnt DESC " +
                "LIMIT 5")
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    // ── Delay stats ───────────────────────────────────────────────────────

    /**
     * Top-5 routes by delayed shipments, with aggregated delay metrics.
     *
     * Returns {@code List<Object[]>}:
     * {@code [originUnlocode, destUnlocode, total, delayed, avgDelay]}.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> delayStatsByRoute(UUID tenantId) {
        return em.createNativeQuery(
                "SELECT op.unlocode, dp.unlocode, " +
                "       COUNT(s.id) AS total, " +
                "       SUM(CASE WHEN s.delay_days > 0 THEN 1 ELSE 0 END) AS delayed, " +
                "       COALESCE(AVG(s.delay_days::float), 0) AS avg_delay " +
                "FROM shipments s " +
                "JOIN ports op ON s.origin_port_id = op.id " +
                "JOIN ports dp ON s.destination_port_id = dp.id " +
                "WHERE s.tenant_id = :tenantId " +
                "GROUP BY op.unlocode, dp.unlocode " +
                "ORDER BY delayed DESC " +
                "LIMIT 5")
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    /**
     * Top-5 vessels by delayed shipments, with aggregated delay metrics.
     *
     * Returns {@code List<Object[]>}:
     * {@code [vesselName, imo, total, delayed, avgDelay]}.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> delayStatsByVessel(UUID tenantId) {
        return em.createNativeQuery(
                "SELECT v.name, v.imo, " +
                "       COUNT(s.id) AS total, " +
                "       SUM(CASE WHEN s.delay_days > 0 THEN 1 ELSE 0 END) AS delayed, " +
                "       COALESCE(AVG(s.delay_days::float), 0) AS avg_delay " +
                "FROM shipments s " +
                "JOIN voyages vo ON s.voyage_id = vo.id " +
                "JOIN vessels v  ON vo.vessel_id = v.id " +
                "WHERE s.tenant_id = :tenantId " +
                "GROUP BY v.id, v.name, v.imo " +
                "ORDER BY delayed DESC " +
                "LIMIT 5")
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    // ── Performance stats ─────────────────────────────────────────────────

    /**
     * Total shipments in DELIVERED status.
     */
    public long countDelivered(UUID tenantId) {
        return (Long) em.createQuery(
                "SELECT COUNT(s) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId AND s.status = :status")
                .setParameter("tenantId", tenantId)
                .setParameter("status", ShipmentStatus.DELIVERED)
                .getSingleResult();
    }

    /**
     * Total shipments in CANCELLED status.
     */
    public long countCancelled(UUID tenantId) {
        return (Long) em.createQuery(
                "SELECT COUNT(s) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId AND s.status = :status")
                .setParameter("tenantId", tenantId)
                .setParameter("status", ShipmentStatus.CANCELLED)
                .getSingleResult();
    }

    /**
     * Average delay days across all non-cancelled shipments.
     * Returns 0.0 if no shipments exist.
     */
    public double avgDelayDaysAll(UUID tenantId) {
        Object result = em.createQuery(
                "SELECT AVG(s.delayDays) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId " +
                "  AND s.status <> :cancelled")
                .setParameter("tenantId", tenantId)
                .setParameter("cancelled", ShipmentStatus.CANCELLED)
                .getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    /**
     * DELIVERED shipments with delayDays ≤ 0 (on-time arrivals).
     */
    public long countDeliveredOnTime(UUID tenantId) {
        return (Long) em.createQuery(
                "SELECT COUNT(s) FROM Shipment s " +
                "WHERE s.tenant.id = :tenantId " +
                "  AND s.status = :status " +
                "  AND (s.delayDays IS NULL OR s.delayDays <= 0)")
                .setParameter("tenantId", tenantId)
                .setParameter("status", ShipmentStatus.DELIVERED)
                .getSingleResult();
    }

    /**
     * On-time delivery rate per carrier (as percentage).
     *
     * Returns {@code List<Object[]>}:
     * {@code [carrierName, onTimePct]} — only carriers with ≥1 DELIVERED shipment.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> onTimeRateByCarrier(UUID tenantId) {
        return em.createNativeQuery(
                "SELECT v.carrier, " +
                "       COUNT(CASE WHEN s.delay_days <= 0 OR s.delay_days IS NULL THEN 1 END)" +
                "         * 100.0 / COUNT(s.id) AS on_time_pct " +
                "FROM shipments s " +
                "JOIN voyages vo ON s.voyage_id = vo.id " +
                "JOIN vessels v  ON vo.vessel_id = v.id " +
                "WHERE s.tenant_id = :tenantId " +
                "  AND s.status = 'DELIVERED' " +
                "  AND v.carrier IS NOT NULL " +
                "GROUP BY v.carrier")
                .setParameter("tenantId", tenantId)
                .getResultList();
    }
}
