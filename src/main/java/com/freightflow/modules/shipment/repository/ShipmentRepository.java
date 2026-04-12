package com.freightflow.modules.shipment.repository;

import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    interface VoyageShipmentCountView {
        UUID getVoyageId();
        long getShipmentCount();
    }

    Optional<Shipment> findByBooking(String booking);

    boolean existsByBooking(String booking);

    boolean existsByBookingAndTenantId(String booking, UUID tenantId);

    Page<Shipment> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Shipment> findByTenantIdAndStatus(UUID tenantId, ShipmentStatus status, Pageable pageable);

    /** CLIENT role: filtra apenas embarques do cliente vinculado ao usuário. */
    Page<Shipment> findByTenantIdAndCustomerId(UUID tenantId, UUID customerId, Pageable pageable);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, ShipmentStatus status);

    /**
     * Embarques com delay_days > 0 e ainda em trânsito (não finalizados).
     */
    @Query("SELECT COUNT(s) FROM Shipment s " +
           "WHERE s.tenant.id = :tenantId " +
           "AND s.status = :status " +
           "AND s.delayDays > 0")
    long countDelayed(@Param("tenantId") UUID tenantId,
                      @Param("status") ShipmentStatus status);

    /**
     * Embarques com risk_level HIGH ou CRITICAL.
     */
    @Query("SELECT COUNT(s) FROM Shipment s " +
           "WHERE s.tenant.id = :tenantId " +
           "AND s.riskLevel IN ('HIGH', 'CRITICAL')")
    long countAtRisk(@Param("tenantId") UUID tenantId);

    @Query("SELECT s FROM Shipment s " +
           "JOIN FETCH s.voyage v " +
           "JOIN FETCH v.vessel " +
           "JOIN FETCH s.originPort " +
           "JOIN FETCH s.destinationPort " +
           "WHERE s.booking = :booking")
    Optional<Shipment> findByBookingWithDetails(@Param("booking") String booking);

    /**
     * Busca shipment por id garantindo isolamento de tenant.
     * Usado em getById() para evitar vazamento cross-tenant.
     */
    Optional<Shipment> findByIdAndTenantId(UUID id, UUID tenantId);

    // ==================== Fleet Map — "My shipments" ====================

    /**
     * Embarques de uma voyage filtrados por tenant.
     * Portos e voyage são eager-fetched para evitar N+1 no ShipmentSummaryResponse.
     * Alertas são lazy-loaded dentro da transação quando deriveRiskLevel() é chamado.
     */
    @Query("""
        SELECT s FROM Shipment s
        JOIN FETCH s.originPort
        JOIN FETCH s.destinationPort
        JOIN FETCH s.voyage
        WHERE s.voyage.id = :voyageId
          AND s.tenant.id = :tenantId
        ORDER BY s.createdAt ASC
    """)
    List<Shipment> findByVoyageIdAndTenantId(
            @Param("voyageId") UUID voyageId,
            @Param("tenantId") UUID tenantId);

    /**
     * Versão para CLIENT role: adicional filtro por customerId.
     */
    @Query("""
        SELECT s FROM Shipment s
        JOIN FETCH s.originPort
        JOIN FETCH s.destinationPort
        JOIN FETCH s.voyage
        WHERE s.voyage.id   = :voyageId
          AND s.tenant.id   = :tenantId
          AND s.customer.id = :customerId
        ORDER BY s.createdAt ASC
    """)
    List<Shipment> findByVoyageIdAndTenantIdAndCustomerId(
            @Param("voyageId")   UUID voyageId,
            @Param("tenantId")   UUID tenantId,
            @Param("customerId") UUID customerId);

    /** Contagem de embarques de um tenant em uma voyage. Usado no VesselService. */
    long countByVoyageIdAndTenantId(UUID voyageId, UUID tenantId);

    @Query("""
        SELECT s.voyage.id AS voyageId, COUNT(s) AS shipmentCount
        FROM Shipment s
        WHERE s.voyage.id IN :voyageIds
          AND s.tenant.id = :tenantId
        GROUP BY s.voyage.id
    """)
    List<VoyageShipmentCountView> countByVoyageIdsAndTenantId(
            @Param("voyageIds") List<UUID> voyageIds,
            @Param("tenantId") UUID tenantId);

    @Query("""
        SELECT s.voyage.id AS voyageId, COUNT(s) AS shipmentCount
        FROM Shipment s
        WHERE s.voyage.id IN :voyageIds
          AND s.tenant.id = :tenantId
          AND s.customer.id = :customerId
        GROUP BY s.voyage.id
    """)
    List<VoyageShipmentCountView> countByVoyageIdsAndTenantIdAndCustomerId(
            @Param("voyageIds") List<UUID> voyageIds,
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId);
}
