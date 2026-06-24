package com.freightflow.modules.shipment.dto;

/**
 * Query parameters aceitos pelo GET /api/v1/shipments.
 *
 * Todos os campos são opcionais (null = sem filtro).
 *
 * - booking          → busca parcial ILIKE '%valor%'
 * - status           → exato (nome do enum ShipmentStatus)
 * - carrier          → busca parcial ILIKE no vessel.carrier (join voyage → vessel)
 * - vesselName       → busca parcial ILIKE no vessel.name   (join voyage → vessel)
 * - originPortUnlocode → exato no originPort.unlocode
 * - riskLevel        → exato no campo String shipment.riskLevel (LOW/MEDIUM/HIGH/CRITICAL)
 */
public record ShipmentFilterParams(
        String booking,
        String status,
        String carrier,
        String vesselName,
        String originPortUnlocode,
        String riskLevel
) {}
