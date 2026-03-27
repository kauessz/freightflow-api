package com.freightflow.modules.csvimport.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Resultado da importacao CSV de shipments.
 * Separa registros importados com sucesso dos que falharam.
 */
public record ImportResult(
    int totalRows,
    int successCount,
    int errorCount,
    List<ImportedShipment> imported,
    List<ImportError> errors,
    Instant processedAt
) {

    public static ImportResult of(List<ImportedShipment> imported, List<ImportError> errors, int totalRows) {
        return new ImportResult(
            totalRows,
            imported.size(),
            errors.size(),
            imported,
            errors,
            Instant.now()
        );
    }

    /**
     * Shipment importado com sucesso.
     */
    public record ImportedShipment(
        int row,
        UUID shipmentId,
        String booking,
        String containerNumber,
        String voyageNumber,
        String originUnlocode,
        String destinationUnlocode,
        String status
    ) {}

    /**
     * Erro de importacao com detalhes da linha e motivo.
     */
    public record ImportError(
        int row,
        String booking,
        List<String> errors
    ) {}
}
