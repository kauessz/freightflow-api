package com.freightflow.modules.shipment.dto;

/**
 * Agregado de KPIs do tenant para o dashboard.
 *
 * @param total      total de embarques do tenant
 * @param inTransit  com status IN_TRANSIT
 * @param arrived    com status ARRIVED
 * @param delayed    ETA já passou e ainda não chegou (não é ARRIVED/DELIVERED/GATE_OUT/CANCELLED)
 * @param atRisk     IN_TRANSIT com ETA em menos de 48 horas
 */
public record ShipmentStatsResponse(
    long total,
    long inTransit,
    long arrived,
    long delayed,
    long atRisk
) {}
