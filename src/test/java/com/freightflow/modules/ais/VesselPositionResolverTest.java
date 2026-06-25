package com.freightflow.modules.ais;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.PositionSource;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.enums.VoyageStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for VesselPositionResolver resolution logic.
 *
 * AisClient is mocked — cache behaviour (Redis TTL, Spring @Cacheable) is
 * the responsibility of Spring's caching infrastructure and is not tested here.
 * Integration tests that require a real cache layer should use Testcontainers.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VesselPositionResolver")
class VesselPositionResolverTest {

    @Mock
    private AisClient aisClient;

    @InjectMocks
    private VesselPositionResolver vesselPositionResolver;

    @Test
    @DisplayName("should return LIVE when AIS returns a valid real position")
    void should_returnLive_when_aisReturnsValidPosition() {
        Voyage voyage = TestDataFactory.voyage();
        AisPositionResponse live = AisPositionResponse.live(
                voyage.getVessel().getImo(),
                -22.90,
                -43.17,
                13.5,
                90.0,
                "underway",
                java.time.Instant.now()
        );

        when(aisClient.getPosition(voyage.getVessel().getImo())).thenReturn(live);

        AisPositionResponse result = vesselPositionResolver.resolveForVoyage(voyage, true);

        assertThat(result).isEqualTo(live);
        assertThat(result.positionSource()).isEqualTo(PositionSource.LIVE);
        assertThat(result.positionEstimated()).isFalse();
        assertThat(result.hasCoordinates()).isTrue();
    }

    @Test
    @DisplayName("should return ESTIMATED at origin port when AIS fails for departed voyage")
    void should_returnEstimatedAtOrigin_when_aisFailsForDepartedVoyage() {
        Voyage voyage = TestDataFactory.voyage();
        voyage.setStatus(VoyageStatus.DEPARTED);
        when(aisClient.getPosition(voyage.getVessel().getImo())).thenReturn(null);

        AisPositionResponse result = vesselPositionResolver.resolveForVoyage(voyage, true);

        assertThat(result.positionSource()).isEqualTo(PositionSource.ESTIMATED);
        assertThat(result.positionEstimated()).isTrue();
        assertThat(result.latitude()).isEqualTo(voyage.getOriginPort().getLatitude());
        assertThat(result.longitude()).isEqualTo(voyage.getOriginPort().getLongitude());
    }

    @Test
    @DisplayName("should not interpolate inland when AIS fails for long route like Santos to Manaus")
    void should_notInterpolateInland_when_aisFailsForLongRoute() throws Exception {
        Voyage voyage = TestDataFactory.voyage();
        Port manaus = new Port("BRMAO", "Manaus", "BR", "America/Manaus", -3.1190, -60.0217);
        setField(voyage, "destinationPort", manaus);
        voyage.setStatus(VoyageStatus.IN_TRANSIT);
        when(aisClient.getPosition(voyage.getVessel().getImo())).thenReturn(null);

        AisPositionResponse result = vesselPositionResolver.resolveForVoyage(voyage, true);

        assertThat(result.positionSource()).isEqualTo(PositionSource.ESTIMATED);
        assertThat(result.positionEstimated()).isTrue();
        assertThat(result.latitude()).isEqualTo(voyage.getOriginPort().getLatitude());
        assertThat(result.longitude()).isEqualTo(voyage.getOriginPort().getLongitude());
        assertThat(result.latitude()).isNotEqualTo((-23.9536 + -3.1190) / 2.0);
    }

    @Test
    @DisplayName("should return UNAVAILABLE when AIS returns null and origin port has no valid coordinates")
    void should_returnUnavailable_when_aisFailsAndPortsCannotEstimate() throws Exception {
        Voyage voyage = TestDataFactory.voyage();
        Port originWithoutCoordinates = TestDataFactory.port(
                TestDataFactory.defaultPortOriginId(), "BRSSZ", "Santos", "BR");
        setField(originWithoutCoordinates, "latitude", null);
        setField(originWithoutCoordinates, "longitude", null);
        setField(voyage, "originPort", originWithoutCoordinates);

        when(aisClient.getPosition(voyage.getVessel().getImo())).thenReturn(null);

        AisPositionResponse result = vesselPositionResolver.resolveForVoyage(voyage, true);

        assertThat(result.positionSource()).isEqualTo(PositionSource.UNAVAILABLE);
        assertThat(result.hasCoordinates()).isFalse();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
