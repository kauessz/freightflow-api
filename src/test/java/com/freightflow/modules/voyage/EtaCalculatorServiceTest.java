package com.freightflow.modules.voyage;

import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.PositionSource;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.vessel.Vessel;
import com.freightflow.modules.vessel.enums.VesselType;
import com.freightflow.modules.voyage.dto.RevisedEtaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.freightflow.fixtures.TestDataFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EtaCalculatorService}.
 *
 * Pure logic tests — no Spring context, no Mockito mocks.
 * The service is instantiated directly with {@code new EtaCalculatorService()}.
 *
 * Real-world port coordinates used:
 *   Santos  (BRSSZ): lat -23.9618, lon -46.3322
 *   Rotterdam (NLRTM): lat  51.9244, lon   4.4777
 */
@DisplayName("EtaCalculatorService")
class EtaCalculatorServiceTest {

    // ── Real port coordinates ─────────────────────────────────────────────

    private static final double SANTOS_LAT    = -23.9618;
    private static final double SANTOS_LON    = -46.3322;
    private static final double ROTTERDAM_LAT =  51.9244;
    private static final double ROTTERDAM_LON =   4.4777;

    // ── SUT ───────────────────────────────────────────────────────────────

    private EtaCalculatorService etaCalculatorService;

    @BeforeEach
    void setUp() {
        etaCalculatorService = new EtaCalculatorService();
    }

    // ── Tests ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Santos → Rotterdam: great-circle distance should be between 5000 and 6000 NM")
    void should_calculateDistance_betweenSantosAndRotterdam() {
        // Arrange — vessel at Santos, heading to Rotterdam at 14 kn
        Voyage voyage = buildVoyage(
                buildPort("BRSSZ", SANTOS_LAT, SANTOS_LON),       // origin
                buildPort("NLRTM", ROTTERDAM_LAT, ROTTERDAM_LON),  // destination
                Instant.now().plus(385, ChronoUnit.HOURS)          // original ETA
        );

        // Vessel is currently at Santos (departure point)
        AisPositionResponse position = buildPosition(SANTOS_LAT, SANTOS_LON, 14.0);

        // Act
        RevisedEtaResponse result = etaCalculatorService.calculate(voyage, position);

        // Assert
        assertThat(result.distanceNm())
                .as("Great-circle distance Santos→Rotterdam should be between 5000 and 6000 NM")
                .isBetween(5000.0, 6000.0);

        assertThat(result.speedKnots()).isEqualTo(14.0);
        assertThat(result.positionSource()).isEqualTo(PositionSource.LIVE.name());
    }

    @Test
    @DisplayName("Speed zero (or below 0.5 kn threshold) → default speed 14.0 kn is used")
    void should_useDefaultSpeed_when_speedIsZeroOrTooLow() {
        // Arrange
        Voyage voyage = buildVoyage(
                buildPort("BRSSZ", SANTOS_LAT, SANTOS_LON),
                buildPort("NLRTM", ROTTERDAM_LAT, ROTTERDAM_LON),
                Instant.now().plus(400, ChronoUnit.HOURS)
        );

        // Speed = 0 (vessel anchored or AIS reporting 0)
        AisPositionResponse positionZeroSpeed = buildPosition(SANTOS_LAT, SANTOS_LON, 0.0);

        // Speed = null (AIS data missing)
        AisPositionResponse positionNullSpeed = buildPosition(SANTOS_LAT, SANTOS_LON, null);

        // Act
        RevisedEtaResponse resultZero = etaCalculatorService.calculate(voyage, positionZeroSpeed);
        RevisedEtaResponse resultNull = etaCalculatorService.calculate(voyage, positionNullSpeed);

        // Assert — both should use the default 14.0 kn
        assertThat(resultZero.speedKnots())
                .as("Speed 0.0 should fall back to default 14.0 kn")
                .isEqualTo(14.0);

        assertThat(resultNull.speedKnots())
                .as("Null speed should fall back to default 14.0 kn")
                .isEqualTo(14.0);
    }

    @Test
    @DisplayName("Vessel at destination: distance ≈ 0, revisedEta ≈ now(), delayDays = 0")
    void should_returnZeroDelay_when_vesselIsAtDestination() {
        // Arrange — vessel is AT Rotterdam (destination)
        Instant originalEta = Instant.now().plus(100, ChronoUnit.HOURS);
        Voyage voyage = buildVoyage(
                buildPort("BRSSZ", SANTOS_LAT, SANTOS_LON),
                buildPort("NLRTM", ROTTERDAM_LAT, ROTTERDAM_LON),
                originalEta
        );

        // Current position = destination (distance ≈ 0)
        AisPositionResponse position = buildPosition(ROTTERDAM_LAT, ROTTERDAM_LON, 14.0);

        // Act
        RevisedEtaResponse result = etaCalculatorService.calculate(voyage, position);

        // Assert
        assertThat(result.distanceNm())
                .as("Distance should be nearly zero when vessel is at destination")
                .isLessThan(1.0);

        assertThat(result.delayDays())
                .as("delayDays must be 0 when vessel has arrived")
                .isEqualTo(0);

        assertThat(result.delayHours())
                .as("delayHours should be negative (vessel arrived early vs. original ETA)")
                .isNegative();

        // revisedEta must be close to now() (within 5 minutes)
        Instant revisedEtaInstant = result.revisedEta().toInstant(java.time.ZoneOffset.UTC);
        assertThat(revisedEtaInstant)
                .as("revisedEta should be close to now()")
                .isBetween(Instant.now().minusSeconds(300), Instant.now().plusSeconds(300));
    }

    @Test
    @DisplayName("Vessel ahead of schedule: delayHours negative, delayDays = 0")
    void should_returnNegativeDelayHours_and_zeroDays_when_vesselIsAhead() {
        // Arrange — originalEta is far in the future, vessel is near destination
        Instant originalEta = Instant.now().plus(200, ChronoUnit.HOURS);
        Voyage voyage = buildVoyage(
                buildPort("BRSSZ", SANTOS_LAT, SANTOS_LON),
                buildPort("NLRTM", ROTTERDAM_LAT, ROTTERDAM_LON),
                originalEta
        );

        // Vessel is only ~50 NM from Rotterdam (just off the Dutch coast) at 20 kn
        // Approx position: shift Rotterdam slightly west
        AisPositionResponse position = buildPosition(51.9244, 3.0000, 20.0);

        // Act
        RevisedEtaResponse result = etaCalculatorService.calculate(voyage, position);

        // Assert
        assertThat(result.delayHours())
                .as("Vessel is close to destination and originalEta is 200h away → delayHours must be negative")
                .isNegative();

        assertThat(result.delayDays())
                .as("delayDays must be 0 when the vessel is ahead of schedule")
                .isEqualTo(0);

        assertThat(result.distanceNm())
                .as("Small remaining distance when vessel is near Rotterdam")
                .isLessThan(100.0);

        assertThat(result.speedKnots()).isEqualTo(20.0);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private Port buildPort(String unlocode, double lat, double lon) {
        return new Port(unlocode, unlocode + " Port", "XX", "UTC", lat, lon);
    }

    private Vessel buildVessel() {
        return new Vessel("9321483", "Test Vessel", "PA", VesselType.CONTAINER, 10000);
    }

    private Voyage buildVoyage(Port origin, Port destination, Instant eta) {
        Instant etd = Instant.now().minusSeconds(3600);
        Voyage voyage = new Voyage("TEST-001", buildVessel(), origin, destination, etd, eta);
        // EtaCalculatorService calls voyage.getId().toString() — must be non-null
        TestDataFactory.setEntityId(voyage, UUID.randomUUID());
        return voyage;
    }

    private AisPositionResponse buildPosition(double lat, double lon, Double speed) {
        // Use the static live() factory when speed is non-null, otherwise build manually
        if (speed != null) {
            return AisPositionResponse.live(
                    "9321483", lat, lon, speed, 270.0, "underway", Instant.now());
        }
        // Null speed — build via the live factory but override speed to null
        return new AisPositionResponse(
                "9321483", lat, lon, null, 270.0,
                "underway", Instant.now(), PositionSource.LIVE, false);
    }
}
