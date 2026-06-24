package com.freightflow.modules.voyage;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.voyage.dto.FleetMapIneligibilityReason;
import com.freightflow.modules.voyage.enums.VoyageStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VoyageFleetMapEligibilityService")
class VoyageFleetMapEligibilityServiceTest {

    private final VoyageFleetMapEligibilityService service = new VoyageFleetMapEligibilityService();

    @Test
    @DisplayName("should_markEligible_when_voyageHasImoPortsScheduleActiveStatusAndShipments")
    void should_markEligible_when_voyageHasImoPortsScheduleActiveStatusAndShipments() {
        var voyage = TestDataFactory.voyage();
        voyage.setStatus(VoyageStatus.IN_TRANSIT);
        voyage.setActive(true);

        var result = service.evaluate(voyage, 1);

        assertThat(result.eligibleForFleetMap()).isTrue();
        assertThat(result.ineligibilityReasons()).isEmpty();
    }

    @Test
    @DisplayName("should_markIneligible_when_vesselHasNoImo")
    void should_markIneligible_when_vesselHasNoImo() {
        var voyage = TestDataFactory.voyage();
        voyage.setStatus(VoyageStatus.IN_TRANSIT);
        voyage.getVessel().setImo(null);

        var result = service.evaluate(voyage, 1);

        assertThat(result.eligibleForFleetMap()).isFalse();
        assertThat(result.ineligibilityReasons()).contains(FleetMapIneligibilityReason.MISSING_IMO);
    }

    @Test
    @DisplayName("should_markIneligible_when_portsOrScheduleAreMissing")
    void should_markIneligible_when_portsOrScheduleAreMissing() throws Exception {
        var voyage = TestDataFactory.voyage();
        voyage.setStatus(VoyageStatus.IN_TRANSIT);
        setField(voyage, "originPort", null);
        setField(voyage, "destinationPort", null);
        setField(voyage, "etd", null);

        var result = service.evaluate(voyage, 1);

        assertThat(result.eligibleForFleetMap()).isFalse();
        assertThat(result.ineligibilityReasons()).contains(
                FleetMapIneligibilityReason.MISSING_ORIGIN_PORT,
                FleetMapIneligibilityReason.MISSING_DESTINATION_PORT,
                FleetMapIneligibilityReason.MISSING_SCHEDULE
        );
    }

    @Test
    @DisplayName("should_markIneligible_when_noLinkedShipmentsOrInactive")
    void should_markIneligible_when_noLinkedShipmentsOrInactive() {
        var voyage = TestDataFactory.voyage();
        voyage.setStatus(VoyageStatus.SCHEDULED);
        voyage.setActive(false);

        var result = service.evaluate(voyage, 0);

        assertThat(result.eligibleForFleetMap()).isFalse();
        assertThat(result.ineligibilityReasons()).contains(
                FleetMapIneligibilityReason.NO_LINKED_SHIPMENTS,
                FleetMapIneligibilityReason.INACTIVE_VOYAGE
        );
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
