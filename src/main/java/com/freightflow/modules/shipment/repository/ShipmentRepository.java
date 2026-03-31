package com.freightflow.modules.shipment.repository;

import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByBooking(String booking);

    boolean existsByBooking(String booking);

    boolean existsByBookingAndTenantId(String booking, UUID tenantId);

    Page<Shipment> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Shipment> findByTenantIdAndStatus(UUID tenantId, ShipmentStatus status, Pageable pageable);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, ShipmentStatus status);

    /**
     * Embarques com ETA já ultrapassada que ainda não chegaram.
     * Exclui statuses finais: ARRIVED, DELIVERED, GATE_OUT, CANCELLED.
     */
    @Query("SELECT COUNT(s) FROM Shipment s JOIN s.voyage v " +
           "WHERE s.tenant.id = :tenantId " +
           "AND v.eta < :now " +
           "AND s.status NOT IN :finishedStatuses")
    long countDelayed(@Param("tenantId") UUID tenantId,
                      @Param("now") Instant now,
                      @Param("finishedStatuses") List<ShipmentStatus> finishedStatuses);

    /**
     * Embarques IN_TRANSIT com ETA entre now e now+48h (em risco de atraso).
     */
    @Query("SELECT COUNT(s) FROM Shipment s JOIN s.voyage v " +
           "WHERE s.tenant.id = :tenantId " +
           "AND s.status = :status " +
           "AND v.eta >= :now " +
           "AND v.eta <= :deadline")
    long countAtRisk(@Param("tenantId") UUID tenantId,
                     @Param("now") Instant now,
                     @Param("deadline") Instant deadline,
                     @Param("status") ShipmentStatus status);

    @Query("SELECT s FROM Shipment s " +
           "JOIN FETCH s.voyage v " +
           "JOIN FETCH v.vessel " +
           "JOIN FETCH s.originPort " +
           "JOIN FETCH s.destinationPort " +
           "WHERE s.booking = :booking")
    Optional<Shipment> findByBookingWithDetails(@Param("booking") String booking);
}
