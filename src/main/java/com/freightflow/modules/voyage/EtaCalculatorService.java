package com.freightflow.modules.voyage;

import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.voyage.dto.RevisedEtaResponse;
import com.freightflow.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * Calculates a revised ETA based on the vessel's current AIS position using the
 * Haversine great-circle distance formula.
 *
 * Algorithm:
 *   1. Compute remaining distance to the destination port (nautical miles).
 *   2. Choose speed: AIS reported speed if > 0.5 kn, otherwise default 14.0 kn.
 *   3. remainingHours = distanceNm / speedKnots
 *   4. revisedEta     = now() + remainingHours
 *   5. Compute delay vs. original voyage ETA.
 *
 * This class has no Spring dependencies — it can be unit-tested with {@code new EtaCalculatorService()}.
 */
@Service
public class EtaCalculatorService {

    private static final Logger log = LoggerFactory.getLogger(EtaCalculatorService.class);

    /** Earth's mean radius in nautical miles. */
    private static final double EARTH_RADIUS_NM = 3440.065;

    /** Default speed used when AIS reports 0 or unavailable (knots). */
    private static final double DEFAULT_SPEED_KN = 14.0;

    /** Minimum AIS speed (kn) below which we fall back to default. */
    private static final double MIN_SPEED_KN = 0.5;

    /**
     * Calculates a revised ETA for the given voyage based on the vessel's current position.
     *
     * @param voyage   the voyage entity (must have destinationPort with coordinates and a non-null eta)
     * @param position the current AIS position (must have valid coordinates)
     * @return a {@link RevisedEtaResponse} with the calculation results
     * @throws BusinessException if destination port coordinates are unavailable
     */
    public RevisedEtaResponse calculate(Voyage voyage, AisPositionResponse position) {
        Double destLat = voyage.getDestinationPort().getLatitude();
        Double destLon = voyage.getDestinationPort().getLongitude();

        if (destLat == null || destLon == null) {
            throw new BusinessException("Destination port '" +
                    voyage.getDestinationPort().getUnlocode() + "' has no coordinates — ETA calculation unavailable");
        }

        double distanceNm = haversineNm(
                position.latitude(), position.longitude(),
                destLat, destLon
        );

        double speedKnots = (position.speed() != null && position.speed() > MIN_SPEED_KN)
                ? position.speed()
                : DEFAULT_SPEED_KN;

        double remainingHours = distanceNm / speedKnots;

        Instant now         = Instant.now();
        Instant revisedEtaI = now.plus((long) (remainingHours * 60), ChronoUnit.MINUTES);
        Instant originalEtaI = voyage.getEta();

        long delayHours = ChronoUnit.HOURS.between(originalEtaI, revisedEtaI);
        int  delayDays  = delayHours > 0 ? (int) Math.ceil(delayHours / 24.0) : 0;

        log.debug("ETA calculation — voyage={}, dist={}nm, speed={}kn, remainingHours={}, delayHours={}",
                voyage.getVoyageNumber(), String.format("%.1f", distanceNm),
                String.format("%.1f", speedKnots), String.format("%.1f", remainingHours), delayHours);

        return new RevisedEtaResponse(
                voyage.getId().toString(),
                voyage.getVoyageNumber(),
                toUtc(originalEtaI),
                toUtc(revisedEtaI),
                Math.round(distanceNm * 10.0) / 10.0,  // 1 decimal place
                Math.round(speedKnots * 10.0) / 10.0,
                delayHours,
                delayDays,
                position.positionSource().name(),
                position.latitude(),
                position.longitude()
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Haversine formula — great-circle distance between two lat/lon points.
     *
     * @return distance in nautical miles
     */
    private double haversineNm(double lat1, double lon1, double lat2, double lon2) {
        double φ1   = Math.toRadians(lat1);
        double φ2   = Math.toRadians(lat2);
        double Δφ   = Math.toRadians(lat2 - lat1);
        double Δλ   = Math.toRadians(lon2 - lon1);

        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2)
                 + Math.cos(φ1) * Math.cos(φ2)
                 * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_NM * c;
    }

    private LocalDateTime toUtc(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
