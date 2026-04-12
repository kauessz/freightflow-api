package com.freightflow.modules.ais;

import com.freightflow.modules.ais.dto.AisPositionResponse;
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

        if (allowEstimatedFallback && hasSafeEstimateBase(voyage)) {
            return AisPositionResponse.estimated(
                    midpoint(voyage.getOriginPort().getLatitude(), voyage.getDestinationPort().getLatitude()),
                    midpoint(voyage.getOriginPort().getLongitude(), voyage.getDestinationPort().getLongitude())
            );
        }

        return AisPositionResponse.unavailable(imo);
    }

    private boolean hasSafeEstimateBase(Voyage voyage) {
        return voyage.getOriginPort() != null
                && voyage.getDestinationPort() != null
                && voyage.getOriginPort().getLatitude() != null
                && voyage.getOriginPort().getLongitude() != null
                && voyage.getDestinationPort().getLatitude() != null
                && voyage.getDestinationPort().getLongitude() != null;
    }

    private double midpoint(Double a, Double b) {
        return (a + b) / 2.0;
    }
}
