package com.freightflow.modules.ais;

import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.PositionSource;
import com.freightflow.modules.event.Event;
import com.freightflow.modules.event.EventRepository;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.enums.VoyageStatus;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scheduled job that polls AIS positions for all IN_TRANSIT and DEPARTED voyages
 * and persists significant position changes as POSITION_UPDATE events.
 *
 * Runs every {@code freightflow.tracking.interval-ms} milliseconds (default 5 minutes).
 * Each voyage is processed independently — a failure on one voyage does not stop the rest.
 *
 * Position format stored in {@code Event.location}: "{lat},{lon}" (6 decimal places).
 * Movement threshold: a new event is only created when the position has shifted
 * more than 0.05 degrees in latitude OR longitude from the last recorded point
 * (approx. 5.5 km at the equator), or when there is no previous POSITION_UPDATE event.
 */
@Component
public class PositionTrackingJob {

    private static final Logger log = LoggerFactory.getLogger(PositionTrackingJob.class);

    /** Minimum degree difference (lat or lon) before recording a new position event. */
    private static final double MOVEMENT_THRESHOLD_DEG = 0.05;

    /** ShipmentStatus values that indicate a shipment is no longer active. */
    private static final List<ShipmentStatus> FINISHED_STATUSES =
            List.of(ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED);

    private final VesselPositionResolver vesselPositionResolver;
    private final EventRepository        eventRepository;
    private final EntityManager          em;

    public PositionTrackingJob(VesselPositionResolver vesselPositionResolver,
                               EventRepository eventRepository,
                               EntityManager em) {
        this.vesselPositionResolver = vesselPositionResolver;
        this.eventRepository        = eventRepository;
        this.em                     = em;
    }

    /**
     * Main entry point for the tracking job.
     *
     * @Transactional ensures lazy collections (Shipments) are accessible within this method.
     * fixedDelay means the next run starts only after the current run finishes,
     * preventing overlapping executions.
     */
    @Scheduled(fixedDelayString = "${freightflow.tracking.interval-ms:300000}")
    @Transactional
    public void trackActiveVoyages() {
        log.info("Position tracking job started");

        List<Voyage> activeVoyages = loadActiveVoyages();

        AtomicInteger eventsCreated = new AtomicInteger(0);

        for (Voyage voyage : activeVoyages) {
            try {
                int created = processVoyage(voyage);
                eventsCreated.addAndGet(created);
            } catch (Exception ex) {
                log.warn("Position tracking failed for voyage={} ({}): {}",
                        voyage.getVoyageNumber(), voyage.getId(), ex.getMessage());
            }
        }

        log.info("Position tracking job finished — {} voyages processed, {} events created",
                activeVoyages.size(), eventsCreated.get());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Loads all IN_TRANSIT and DEPARTED voyages with vessel, originPort, and destinationPort
     * eagerly fetched (required by VesselPositionResolver).
     */
    @SuppressWarnings("unchecked")
    private List<Voyage> loadActiveVoyages() {
        return em.createQuery(
                "SELECT v FROM Voyage v " +
                "JOIN FETCH v.vessel " +
                "JOIN FETCH v.originPort " +
                "JOIN FETCH v.destinationPort " +
                "WHERE v.status IN :statuses",
                Voyage.class)
                .setParameter("statuses", List.of(VoyageStatus.IN_TRANSIT, VoyageStatus.DEPARTED))
                .getResultList();
    }

    /**
     * Processes a single voyage: resolves AIS position and records events for active shipments
     * that have moved beyond the threshold.
     *
     * @return the number of new POSITION_UPDATE events created
     */
    private int processVoyage(Voyage voyage) {
        AisPositionResponse position = vesselPositionResolver.resolveForVoyage(voyage);

        if (position.positionSource() == PositionSource.UNAVAILABLE || !position.hasCoordinates()) {
            return 0;
        }

        List<Shipment> activeShipments = voyage.getShipments().stream()
                .filter(s -> !FINISHED_STATUSES.contains(s.getStatus()))
                .toList();

        if (activeShipments.isEmpty()) {
            return 0;
        }

        String location    = formatLocation(position.latitude(), position.longitude());
        String description = formatDescription(position);

        int created = 0;
        for (Shipment shipment : activeShipments) {
            if (shouldRecordPosition(shipment, position)) {
                Event event = new Event(
                        shipment, EventType.POSITION_UPDATE, location, description, Instant.now());
                eventRepository.save(event);
                created++;
            }
        }
        return created;
    }

    /**
     * Decides whether a new POSITION_UPDATE event should be recorded for this shipment.
     *
     * Returns {@code true} if:
     *   - There is no previous POSITION_UPDATE event for this shipment, OR
     *   - The current position differs from the last recorded position by more than
     *     {@value MOVEMENT_THRESHOLD_DEG} degrees in either lat or lon.
     */
    private boolean shouldRecordPosition(Shipment shipment, AisPositionResponse current) {
        Optional<Event> lastEvent = eventRepository
                .findByShipmentIdOrderByOccurredAtDesc(shipment.getId())
                .stream()
                .filter(e -> e.getType() == EventType.POSITION_UPDATE)
                .findFirst();

        if (lastEvent.isEmpty()) {
            return true;
        }

        try {
            String[] parts = lastEvent.get().getLocation().split(",", 2);
            if (parts.length < 2) return true;

            double prevLat = Double.parseDouble(parts[0].trim());
            double prevLon = Double.parseDouble(parts[1].trim());

            return Math.abs(current.latitude() - prevLat) > MOVEMENT_THRESHOLD_DEG
                || Math.abs(current.longitude() - prevLon) > MOVEMENT_THRESHOLD_DEG;

        } catch (NumberFormatException ex) {
            // Malformed previous location — record new event to repair the track
            log.warn("Could not parse previous position for shipment={}: {}", shipment.getId(), ex.getMessage());
            return true;
        }
    }

    /**
     * Formats latitude and longitude into the canonical storage format "lat,lon"
     * with 6 decimal places (≈ 11 cm precision).
     */
    private String formatLocation(double lat, double lon) {
        return String.format("%.6f,%.6f", lat, lon);
    }

    /**
     * Builds the human-readable event description from the AIS position data.
     * Uses {@code course} as a proxy for heading (course over ground from AIS).
     */
    private String formatDescription(AisPositionResponse position) {
        double speed   = position.speed()  != null ? position.speed()  : 0.0;
        double heading = position.course() != null ? position.course() : 0.0;
        String source  = position.positionSource().name();
        return String.format("Position update — %.1f kn, heading %.0f° (%s)", speed, heading, source);
    }
}
