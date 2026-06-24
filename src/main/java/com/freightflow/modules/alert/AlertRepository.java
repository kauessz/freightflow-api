package com.freightflow.modules.alert;

import com.freightflow.modules.alert.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    /**
     * Todos os alerts de um embarque, ordenados do mais recente ao mais antigo.
     */
    @Query("""
        SELECT a FROM Alert a
        JOIN FETCH a.shipment
        WHERE a.shipment.id = :shipmentId
        ORDER BY a.createdAt DESC
    """)
    List<Alert> findByShipmentId(@Param("shipmentId") UUID shipmentId);

    @Query("""
        SELECT a FROM Alert a
        JOIN FETCH a.shipment s
        WHERE s.id = :shipmentId
          AND s.tenant.id = :tenantId
        ORDER BY a.createdAt DESC
    """)
    List<Alert> findByShipmentIdAndShipmentTenantId(
            @Param("shipmentId") UUID shipmentId,
            @Param("tenantId") UUID tenantId);

    @Query("""
        SELECT a FROM Alert a
        JOIN FETCH a.shipment s
        WHERE s.id = :shipmentId
          AND s.tenant.id = :tenantId
          AND s.customer.id = :customerId
        ORDER BY a.createdAt DESC
    """)
    List<Alert> findByShipmentIdAndShipmentTenantIdAndShipmentCustomerId(
            @Param("shipmentId") UUID shipmentId,
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId);

    /**
     * Alerts em aberto (resolved = false) de todos os embarques do tenant.
     * Faz join através de shipment → tenant para garantir isolamento.
     */
    @Query("""
        SELECT a FROM Alert a
        JOIN FETCH a.shipment s
        WHERE s.tenant.id = :tenantId
          AND a.resolved = false
        ORDER BY a.createdAt DESC
    """)
    List<Alert> findOpenByTenantId(@Param("tenantId") UUID tenantId);

    @Query("""
        SELECT a FROM Alert a
        JOIN FETCH a.shipment s
        WHERE a.id = :alertId
          AND s.tenant.id = :tenantId
    """)
    Optional<Alert> findByIdAndShipmentTenantId(
            @Param("alertId") UUID alertId,
            @Param("tenantId") UUID tenantId);

    @Query("""
        SELECT a FROM Alert a
        JOIN FETCH a.shipment s
        WHERE a.id = :alertId
          AND s.tenant.id = :tenantId
          AND s.customer.id = :customerId
    """)
    Optional<Alert> findByIdAndShipmentTenantIdAndShipmentCustomerId(
            @Param("alertId") UUID alertId,
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId);

    /**
     * Verifica se já existe um alert aberto do mesmo tipo para o embarque.
     * Usado para evitar duplicatas em aberto.
     */
    boolean existsByShipmentIdAndTypeAndResolvedFalse(UUID shipmentId, AlertType type);
}
