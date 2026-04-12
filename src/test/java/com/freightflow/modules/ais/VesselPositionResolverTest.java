package com.freightflow.modules.ais;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.PositionSource;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.voyage.Voyage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VesselPositionResolver")
class VesselPositionResolverTest {

    @Mock
    private AisClient aisClient;

    @InjectMocks
    private VesselPositionResolver vesselPositionResolver;

    @Test
    @DisplayName("should_returnEstimated_when_aisFailsAndPortsHaveCoordinates")
    void should_returnEstimated_when_aisFailsAndPortsHaveCoordinates() {
        Voyage voyage = TestDataFactory.voyage();
        when(aisClient.getPosition(voyage.getVessel().getImo())).thenReturn(null);

        AisPositionResponse result = vesselPositionResolver.resolveForVoyage(voyage, true);

        assertThat(result.positionSource()).isEqualTo(PositionSource.ESTIMATED);
        assertThat(result.positionEstimated()).isTrue();
        assertThat(result.hasCoordinates()).isTrue();
    }

    @Test
    @DisplayName("should_returnUnavailable_when_aisFailsAndPortsCannotEstimate")
    void should_returnUnavailable_when_aisFailsAndPortsCannotEstimate() throws Exception {
        Voyage voyage = TestDataFactory.voyage();
        Port originWithoutCoordinates = TestDataFactory.port(TestDataFactory.defaultPortOriginId(), "BRSSZ", "Santos", "BR");
        setField(originWithoutCoordinates, "latitude", null);
        setField(originWithoutCoordinates, "longitude", null);
        setField(voyage, "originPort", originWithoutCoordinates);

        when(aisClient.getPosition(voyage.getVessel().getImo())).thenReturn(null);

        AisPositionResponse result = vesselPositionResolver.resolveForVoyage(voyage, true);

        assertThat(result.positionSource()).isEqualTo(PositionSource.UNAVAILABLE);
        assertThat(result.hasCoordinates()).isFalse();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
