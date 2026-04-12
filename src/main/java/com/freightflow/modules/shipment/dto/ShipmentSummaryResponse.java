package com.freightflow.modules.shipment.dto;

import com.freightflow.modules.alert.Alert;
import com.freightflow.modules.alert.enums.Severity;
import com.freightflow.modules.shipment.Shipment;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Resumo de embarque usado no painel lateral do Fleet Map e no endpoint
 * GET /api/v1/voyages/{id}/shipments.
 */
public record ShipmentSummaryResponse(
        UUID   id,
        String booking,
        String containerNumber,
        String status,
        String riskLevel,       // NORMAL | LOW | MEDIUM | HIGH | CRITICAL (derivado dos alertas ativos)
        String blStatus,        // documentStatus (PENDING, ISSUED, etc.)
        String customsStatus,
        PortInfo originPort,
        PortInfo destPort,
        Instant eta
) {

    /** Informações mínimas de porto para este response. */
    public record PortInfo(String unlocode, String name) {}

    public static ShipmentSummaryResponse from(Shipment s) {
        return new ShipmentSummaryResponse(
                s.getId(),
                s.getBooking(),
                s.getContainerNumber(),
                s.getStatus().name(),
                deriveRiskLevel(s.getAlerts()),
                s.getDocumentStatus(),
                s.getCustomsStatus(),
                new PortInfo(s.getOriginPort().getUnlocode(), s.getOriginPort().getName()),
                new PortInfo(s.getDestinationPort().getUnlocode(), s.getDestinationPort().getName()),
                s.getVoyage().getEta()
        );
    }

    /**
     * Deriva o riskLevel a partir dos alertas ativos (não resolvidos).
     * Retorna a severidade mais alta, ou "NORMAL" se não houver alertas abertos.
     * Ordem: CRITICAL > HIGH > MEDIUM > LOW > NORMAL
     */
    private static String deriveRiskLevel(List<Alert> alerts) {
        if (alerts == null || alerts.isEmpty()) return "NORMAL";
        return alerts.stream()
                .filter(a -> !a.isResolved())
                .map(Alert::getSeverity)
                .max(Comparator.comparingInt(Severity::ordinal))
                .map(Severity::name)
                .orElse("NORMAL");
    }
}
