package com.freightflow.modules.ais;

import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.voyage.Voyage;
import org.springframework.stereotype.Component;

@Component
public class VesselPositionResolver {

    private final AisClient aisClient;

    public VesselPositionResolver(AisClient aisClient) {
        this.aisClient = aisClient;
    }

    public AisPositionResponse resolveByImo(String imo) {
        AisPositionResponse position = aisClient.getPosition(imo);
        return position != null ? position : AisPositionResponse.unavailable(imo);
    }

    public AisPositionResponse resolveForVoyage(Voyage voyage) {
        return resolveForVoyage(voyage, true);
    }

    public AisPositionResponse resolveForVoyage(Voyage voyage, boolean allowEstimatedFallback) {
        String imo = voyage.getVessel() != null ? voyage.getVessel().getImo() : null;
        AisPositionResponse position = aisClient.getPosition(imo);
        if (position != null) {
            return position;
        }

        if (allowEstimatedFallback) {
            AisPositionResponse estimatedPosition = estimateFromVoyageContext(voyage);
            if (estimatedPosition != null) {
                return estimatedPosition;
            }
        }

        return AisPositionResponse.unavailable(imo);
    }

    private AisPositionResponse estimateFromVoyageContext(Voyage voyage) {
        Port anchorPort = switch (voyage.getStatus()) {
            case ARRIVED, COMPLETED -> voyage.getDestinationPort();
            case CANCELLED -> null;
            case SCHEDULED, DEPARTED, IN_TRANSIT -> voyage.getOriginPort();
        };

        if (anchorPort == null || !PositionCoordinates.isValid(anchorPort.getLatitude(), anchorPort.getLongitude())) {
            return null;
        }

        return AisPositionResponse.estimated(anchorPort.getLatitude(), anchorPort.getLongitude());
    }
}
