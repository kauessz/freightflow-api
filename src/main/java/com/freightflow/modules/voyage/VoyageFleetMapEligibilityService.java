package com.freightflow.modules.voyage;

import com.freightflow.modules.voyage.dto.FleetMapIneligibilityReason;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VoyageFleetMapEligibilityService {

    public record EligibilityResult(boolean eligibleForFleetMap, List<FleetMapIneligibilityReason> ineligibilityReasons) {}

    public EligibilityResult evaluate(Voyage voyage, long linkedShipmentCount) {
        List<FleetMapIneligibilityReason> reasons = new ArrayList<>();

        if (voyage.getVessel() == null || isBlank(voyage.getVessel().getImo())) {
            reasons.add(FleetMapIneligibilityReason.MISSING_IMO);
        }
        if (voyage.getOriginPort() == null) {
            reasons.add(FleetMapIneligibilityReason.MISSING_ORIGIN_PORT);
        }
        if (voyage.getDestinationPort() == null) {
            reasons.add(FleetMapIneligibilityReason.MISSING_DESTINATION_PORT);
        }
        if (voyage.getEtd() == null || voyage.getEta() == null) {
            reasons.add(FleetMapIneligibilityReason.MISSING_SCHEDULE);
        }
        if (linkedShipmentCount <= 0) {
            reasons.add(FleetMapIneligibilityReason.NO_LINKED_SHIPMENTS);
        }
        if (!voyage.isActive() || !isActiveStatus(voyage)) {
            reasons.add(FleetMapIneligibilityReason.INACTIVE_VOYAGE);
        }

        return new EligibilityResult(reasons.isEmpty(), List.copyOf(reasons));
    }

    private boolean isActiveStatus(Voyage voyage) {
        return voyage.getStatus() == com.freightflow.modules.voyage.enums.VoyageStatus.DEPARTED
                || voyage.getStatus() == com.freightflow.modules.voyage.enums.VoyageStatus.IN_TRANSIT;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
