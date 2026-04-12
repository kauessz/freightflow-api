package com.freightflow.modules.vessel.dto;

import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.PositionSource;
import com.freightflow.modules.vessel.Vessel;
import com.freightflow.modules.voyage.Voyage;

import java.time.Instant;
import java.util.UUID;

/**
 * Response do endpoint GET /api/v1/vessels/active-with-shipments.
 * Combina dados do vessel, da voyage ativa e da posição AIS atual,
 * com a contagem de embarques do tenant naquela voyage.
 */
public record VesselWithVoyageResponse(
        UUID    vesselId,
        String  imo,
        String  name,
        String  carrier,
        Double  latitude,
        Double  longitude,
        Instant lastUpdate,
        PositionSource positionSource,
        boolean positionEstimated,
        UUID    voyageId,
        String  voyageNumber,
        String  status,
        String  originPortName,
        String  originPortUnlocode,
        String  destPortName,
        String  destPortUnlocode,
        Instant eta,
        int     shipmentCount
) {

    /**
     * Deriva o carrier/operador a partir do nome do vessel.
     * Espelha a lógica do frontend (fleet-map.tsx → deriveCarrier).
     */
    public static String deriveCarrier(String vesselName) {
        String n = vesselName.toUpperCase();
        if (n.contains("CMA CGM"))                             return "CMA CGM";
        if (n.contains("HMM"))                                 return "HMM";
        if (n.contains("LOG IN") || n.contains("LOG-IN"))      return "Log-In";
        if (n.contains("MAERSK") || n.contains("SAN NICOLAS")) return "Maersk";
        if (n.contains("MSC"))                                  return "MSC";
        if (n.startsWith("ONE") || n.contains(" ONE "))        return "ONE";
        return "Other";
    }

    public static VesselWithVoyageResponse from(Voyage voyage, AisPositionResponse pos, int shipmentCount) {
        Vessel v = voyage.getVessel();

        Double  lat       = pos != null ? pos.latitude()  : null;
        Double  lon       = pos != null ? pos.longitude() : null;
        Instant lastUpdate = pos != null ? pos.lastUpdate() : null;
        PositionSource source = pos != null ? pos.positionSource() : PositionSource.UNAVAILABLE;
        boolean estimated = pos != null && pos.positionEstimated();

        return new VesselWithVoyageResponse(
                v.getId(),
                v.getImo(),
                v.getName(),
                deriveCarrier(v.getName()),
                lat,
                lon,
                lastUpdate,
                source,
                estimated,
                voyage.getId(),
                voyage.getVoyageNumber(),
                voyage.getStatus().name(),
                voyage.getOriginPort().getName(),
                voyage.getOriginPort().getUnlocode(),
                voyage.getDestinationPort().getName(),
                voyage.getDestinationPort().getUnlocode(),
                voyage.getEta(),
                shipmentCount
        );
    }
}
