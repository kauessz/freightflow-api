package com.freightflow.modules.alert;

import com.freightflow.modules.alert.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Verifica se já existe um alert aberto do mesmo tipo para o embarque.
     * Usado para evitar duplicatas em aberto.
     */
    boolean existsByShipmentIdAndTypeAndResolvedFalse(UUID shipmentId, AlertType type);
}
