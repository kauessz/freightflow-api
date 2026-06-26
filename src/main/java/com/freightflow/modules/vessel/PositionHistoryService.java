package com.freightflow.modules.vessel;

import com.freightflow.modules.ais.PositionCoordinates;
import com.freightflow.modules.event.Event;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.vessel.dto.PositionTrackPoint;
import com.freightflow.shared.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Retrieves the position track history for a vessel from stored POSITION_UPDATE events.
 *
 * Events are recorded by {@link com.freightflow.modules.ais.PositionTrackingJob}
 * and stored in the events table with:
 *   type        = POSITION_UPDATE
 *   location    = "{lat},{lon}" (6 decimal places)
 *   description = "Position update — {speed} kn, heading {course}° ({source})"
 */
@Service
@Transactional(readOnly = true)
public class PositionHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PositionHistoryService.class);

    /** Matches the speed value in the event description, e.g. "14.5 kn". */
    private static final Pattern SPEED_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*kn");

    /** Maximum number of track points allowed per request. */
    public static final int MAX_LIMIT = 200;
    public static final int DEFAULT_LIMIT = 50;

    private final ShipmentRepository shipmentRepository;
    private final EntityManager      em;

    public PositionHistoryService(ShipmentRepository shipmentRepository, EntityManager em) {
        this.shipmentRepository = shipmentRepository;
        this.em                 = em;
    }

    /**
     * Returns up to {@code limit} POSITION_UPDATE events for the active shipments
     * of all voyages belonging to the vessel identified by {@code imo}, ordered
     * by event time descending.
     *
     * @param imo   vessel IMO number (throws 404 if not found or not accessible within scope)
     * @param limit maximum number of points to return; capped at {@value MAX_LIMIT}
     */
    public List<PositionTrackPoint> getPositionHistory(String imo, UUID tenantId, UUID customerId, int limit) {
        log.debug("Fetching position history for vessel imo={}, tenantId={}, customerId={}, limit={}",
                imo, tenantId, customerId, limit);

        if (!hasScopedAccess(imo, tenantId, customerId)) {
            throw new ResourceNotFoundException("Vessel", imo);
        }

        int effectiveLimit = sanitizeLimit(limit);

        StringBuilder jpql = new StringBuilder(
                "SELECT e, s.voyage.id FROM Event e JOIN e.shipment s JOIN s.voyage v JOIN v.vessel vessel " +
                "WHERE vessel.imo = :imo " +
                "  AND s.tenant.id = :tenantId " +
                "  AND s.status NOT IN :finished " +
                "  AND e.type = :type ");

        if (customerId != null) {
            jpql.append(" AND s.customer.id = :customerId ");
        }

        jpql.append("ORDER BY e.occurredAt DESC");

        var query = em.createQuery(jpql.toString(), Object[].class)
                .setParameter("imo", imo)
                .setParameter("tenantId", tenantId)
                .setParameter("finished", List.of(ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED))
                .setParameter("type", EventType.POSITION_UPDATE)
                .setMaxResults(effectiveLimit);

        if (customerId != null) {
            query.setParameter("customerId", customerId);
        }

        List<Object[]> rows = query.getResultList();

        return rows.stream()
                .map(row -> parseTrackPoint((Event) row[0], (UUID) row[1]))
                .filter(Objects::nonNull)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Parses a {@link PositionTrackPoint} from an event row.
     * Returns {@code null} if the location field cannot be parsed — the caller filters out nulls.
     */
    private PositionTrackPoint parseTrackPoint(Event event, UUID voyageId) {
        try {
            var coordinates = PositionCoordinates.parseStoredLocation(event.getLocation());
            if (coordinates.isEmpty()) {
                return null;
            }

            double lat = coordinates.get().latitude();
            double lon = coordinates.get().longitude();

            Double speed = extractSpeed(event.getDescription());

            LocalDateTime occurredAt = LocalDateTime.ofInstant(event.getOccurredAt(), ZoneOffset.UTC);

            return new PositionTrackPoint(lat, lon, occurredAt, speed, voyageId.toString());

        } catch (Exception ex) {
            log.debug("Skipping unparseable position event id={}: {}", event.getId(), ex.getMessage());
            return null;
        }
    }

    private boolean hasScopedAccess(String imo, UUID tenantId, UUID customerId) {
        if (customerId != null) {
            return shipmentRepository.existsByVesselImoAndTenantIdAndCustomerId(imo, tenantId, customerId);
        }
        return shipmentRepository.existsByVesselImoAndTenantId(imo, tenantId);
    }

    private int sanitizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /**
     * Extracts the speed value (knots) from an event description.
     * Description format: "Position update — 14.5 kn, heading 270° (LIVE)"
     * Returns {@code null} if the pattern is not found.
     */
    private Double extractSpeed(String description) {
        if (description == null) return null;
        Matcher m = SPEED_PATTERN.matcher(description);
        return m.find() ? Double.parseDouble(m.group(1)) : null;
    }
}
