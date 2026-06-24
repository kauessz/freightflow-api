package com.freightflow.modules.event;

import com.freightflow.modules.event.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.shipment
        WHERE e.shipment.id = :shipmentId
        ORDER BY e.occurredAt DESC
    """)
    List<Event> findByShipmentIdOrderByOccurredAtDesc(@Param("shipmentId") UUID shipmentId);

    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.shipment
        WHERE e.shipment.id = :shipmentId
        ORDER BY e.occurredAt ASC
    """)
    List<Event> findByShipmentIdOrderByOccurredAtAsc(@Param("shipmentId") UUID shipmentId);

    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.shipment s
        WHERE s.id = :shipmentId
          AND s.tenant.id = :tenantId
        ORDER BY e.occurredAt ASC
    """)
    List<Event> findByShipmentIdAndShipmentTenantIdOrderByOccurredAtAsc(
            @Param("shipmentId") UUID shipmentId,
            @Param("tenantId") UUID tenantId);

    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.shipment s
        WHERE s.id = :shipmentId
          AND s.tenant.id = :tenantId
          AND s.customer.id = :customerId
        ORDER BY e.occurredAt ASC
    """)
    List<Event> findByShipmentIdAndShipmentTenantIdAndShipmentCustomerIdOrderByOccurredAtAsc(
            @Param("shipmentId") UUID shipmentId,
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId);

    @Query(value = """
        SELECT e FROM Event e
        JOIN FETCH e.shipment
        WHERE e.shipment.id = :shipmentId
    """,
    countQuery = "SELECT COUNT(e) FROM Event e WHERE e.shipment.id = :shipmentId")
    Page<Event> findByShipmentId(@Param("shipmentId") UUID shipmentId, Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.shipment s
        WHERE e.id = :eventId
          AND s.id = :shipmentId
          AND s.tenant.id = :tenantId
    """)
    Optional<Event> findByIdAndShipmentIdAndShipmentTenantId(
            @Param("eventId") UUID eventId,
            @Param("shipmentId") UUID shipmentId,
            @Param("tenantId") UUID tenantId);

    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.shipment s
        WHERE e.id = :eventId
          AND s.id = :shipmentId
          AND s.tenant.id = :tenantId
          AND s.customer.id = :customerId
    """)
    Optional<Event> findByIdAndShipmentIdAndShipmentTenantIdAndShipmentCustomerId(
            @Param("eventId") UUID eventId,
            @Param("shipmentId") UUID shipmentId,
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId);

    boolean existsByShipmentIdAndType(UUID shipmentId, EventType type);
}
