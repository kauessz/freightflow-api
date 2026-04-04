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
}
