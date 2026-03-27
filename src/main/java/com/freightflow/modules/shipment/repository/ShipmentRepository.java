package com.freightflow.modules.shipment.repository;

import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByBooking(String booking);

    boolean existsByBooking(String booking);

    boolean existsByBookingAndTenantId(String booking, UUID tenantId);

    Page<Shipment> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Shipment> findByTenantIdAndStatus(UUID tenantId, ShipmentStatus status, Pageable pageable);

    @Query("SELECT s FROM Shipment s " +
           "JOIN FETCH s.voyage v " +
           "JOIN FETCH v.vessel " +
           "JOIN FETCH s.originPort " +
           "JOIN FETCH s.destinationPort " +
           "WHERE s.booking = :booking")
    Optional<Shipment> findByBookingWithDetails(String booking);
}
