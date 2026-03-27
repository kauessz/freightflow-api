package com.freightflow.modules.event;

import com.freightflow.modules.event.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(value = """
        SELECT e FROM Event e
        JOIN FETCH e.shipment
        WHERE e.shipment.id = :shipmentId
    """,
    countQuery = "SELECT COUNT(e) FROM Event e WHERE e.shipment.id = :shipmentId")
    Page<Event> findByShipmentId(@Param("shipmentId") UUID shipmentId, Pageable pageable);

    boolean existsByShipmentIdAndType(UUID shipmentId, EventType type);
}
