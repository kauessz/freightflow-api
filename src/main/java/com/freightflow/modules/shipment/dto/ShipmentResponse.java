package com.freightflow.modules.shipment.dto;

import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.shipment.enums.ContainerType;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.Instant;

public record ShipmentResponse(
    UUID id,
    String booking,
    // Documentos
    String houseBl,
    String masterBl,
    String customerReference,
    // Container
    String containerNumber,
    ContainerType containerType,
    Integer containerSizeFt,
    String containerIsoCode,
    BigDecimal grossWeightKg,
    BigDecimal netWeightKg,
    BigDecimal volumeCbm,
    Integer packages,
    String packageType,
    // Status
    ShipmentStatus status,
    String documentStatus,
    String customsStatus,
    String riskLevel,
    Integer delayDays,
    // Portos
    String originPortName,
    String originPortUnlocode,
    String destinationPortName,
    String destinationPortUnlocode,
    String transshipmentPortName,
    String transshipmentPortUnlocode,
    // Partes
    String shipper,
    String consignee,
    String notifyParty,
    String operatorName,
    // Voyage
    String vesselName,
    String voyageNumber,
    String carrier,
    String serviceLane,
    Instant eta,
    // Comercial
    String incoterm,
    String freightTerm,
    String cargoDescription,
    // Misc
    String vesselSourceUrl,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {
    public static ShipmentResponse from(Shipment s) {
        String tsPortName = s.getTransshipmentPort() != null ? s.getTransshipmentPort().getName() : null;
        String tsPortCode = s.getTransshipmentPort() != null ? s.getTransshipmentPort().getUnlocode() : null;
        String vesselName = s.getVoyage().getVessel().getName();
        String explicitCarrier = s.getVoyage().getVessel().getCarrier();
        String carrier = (explicitCarrier != null && !explicitCarrier.isBlank())
                ? explicitCarrier
                : deriveCarrier(vesselName);

        return new ShipmentResponse(
            s.getId(),
            s.getBooking(),
            s.getHouseBl(),
            s.getMasterBl(),
            s.getCustomerReference(),
            s.getContainerNumber(),
            s.getContainerType(),
            s.getContainerSizeFt(),
            s.getContainerIsoCode(),
            s.getGrossWeightKg(),
            s.getNetWeightKg(),
            s.getVolumeCbm(),
            s.getPackages(),
            s.getPackageType(),
            s.getStatus(),
            s.getDocumentStatus(),
            s.getCustomsStatus(),
            s.getRiskLevel(),
            s.getDelayDays(),
            s.getOriginPort().getName(),
            s.getOriginPort().getUnlocode(),
            s.getDestinationPort().getName(),
            s.getDestinationPort().getUnlocode(),
            tsPortName,
            tsPortCode,
            s.getShipper(),
            s.getConsignee(),
            s.getNotifyParty(),
            s.getOperatorName(),
            vesselName,
            s.getVoyage().getVoyageNumber(),
            carrier,
            s.getServiceLane(),
            s.getVoyage().getEta(),
            s.getIncoterm(),
            s.getFreightTerm(),
            s.getCargoDescription(),
            s.getVesselSourceUrl(),
            s.getNotes(),
            s.getCreatedAt(),
            s.getUpdatedAt()
        );
    }

    private static String deriveCarrier(String vesselName) {
        if (vesselName == null) return "Unknown";
        String upper = vesselName.toUpperCase();
        if (upper.startsWith("CMA CGM") || upper.startsWith("CMA-CGM")) return "CMA CGM";
        if (upper.startsWith("HMM")) return "HMM";
        if (upper.startsWith("LOG IN") || upper.startsWith("LOG-IN")) return "Log-In";
        if (upper.startsWith("MAERSK") || upper.contains("MAERSK")) return "Maersk";
        if (upper.startsWith("MSC")) return "MSC";
        if (upper.startsWith("ONE ") || upper.equals("ONE")) return "ONE";
        if (upper.startsWith("SAN NICOLAS")) return "Maersk";
        return "Other";
    }
}
