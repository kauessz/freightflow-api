package com.freightflow.modules.vessel;

import com.freightflow.modules.event.Event;
import com.freightflow.modules.event.enums.EventType;
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

    private final VesselRepository vesselRepository;
    private final EntityManager    em;

    public PositionHistoryService(VesselRepository vesselRepository, EntityManager em) {
        this.vesselRepository = vesselRepository;
        this.em               = em;
    }

    /**
     * Returns up to {@code limit} POSITION_UPDATE events for the active shipments
     * of all voyages belonging to the vessel identified by {@code imo}, ordered
     * by event time descending.
     *
     * @param imo   vessel IMO number (throws 404 if not found)
     * @param limit maximum number of points to return; capped at {@value MAX_LIMIT}
     */
    public List<PositionTrackPoint> getPositionHistory(String imo, int limit) {
        log.debug("Fetching position history for vessel imo={}, limit={}", imo, limit);

        Vessel vessel = vesselRepository.findByImo(imo)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", imo));

        int effectiveLimit = Math.min(limit, MAX_LIMIT);

        // Single JPQL join across Event → Shipment → Voyage → Vessel, filtered by status.
        // Returns [Event, voyageId] pairs ordered by occurredAt DESC.
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                "SELECT e, s.voyage.id FROM Event e JOIN e.shipment s " +
                "WHERE s.voyage.vessel.id = :vesselId " +
                "  AND s.status NOT IN :finished " +
                "  AND e.type = :type " +
                "ORDER BY e.occurredAt DESC")
                .setParameter("vesselId", vessel.getId())
                .setParameter("finished", List.of(ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED))
                .setParameter("type", EventType.POSITION_UPDATE)
                .setMaxResults(effectiveLimit)
                .getResultList();

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
            String[] coords = event.getLocation().split(",", 2);
            if (coords.length < 2) return null;

            double lat = Double.parseDouble(coords[0].trim());
            double lon = Double.parseDouble(coords[1].trim());

            Double speed = extractSpeed(event.getDescription());

            LocalDateTime occurredAt = LocalDateTime.ofInstant(event.getOccurredAt(), ZoneOffset.UTC);

            return new PositionTrackPoint(lat, lon, occurredAt, speed, voyageId.toString());

        } catch (Exception ex) {
            log.debug("Skipping unparseable position event id={}: {}", event.getId(), ex.getMessage());
            return null;
        }
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
