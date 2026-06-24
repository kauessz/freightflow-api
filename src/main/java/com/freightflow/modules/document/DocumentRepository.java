package com.freightflow.modules.document;

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
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /**
     * Returns all active documents linked to a shipment, ordered by the default
     * entity order (uploaded_at DESC via the SQL index direction).
     */
    List<Document> findByShipmentIdAndActiveTrue(UUID shipmentId);

    @Query("""
        SELECT d FROM Document d
        JOIN FETCH d.shipment s
        WHERE s.id = :shipmentId
          AND s.tenant.id = :tenantId
          AND d.active = true
        ORDER BY d.uploadedAt DESC
    """)
    List<Document> findByShipmentIdAndShipmentTenantIdAndActiveTrue(
            @Param("shipmentId") UUID shipmentId,
            @Param("tenantId") UUID tenantId);

    @Query("""
        SELECT d FROM Document d
        JOIN FETCH d.shipment s
        WHERE s.id = :shipmentId
          AND s.tenant.id = :tenantId
          AND s.customer.id = :customerId
          AND d.active = true
        ORDER BY d.uploadedAt DESC
    """)
    List<Document> findByShipmentIdAndShipmentTenantIdAndShipmentCustomerIdAndActiveTrue(
            @Param("shipmentId") UUID shipmentId,
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId);

    @Query("""
        SELECT d FROM Document d
        JOIN FETCH d.shipment s
        WHERE d.id = :documentId
          AND s.tenant.id = :tenantId
          AND d.active = true
    """)
    Optional<Document> findByIdAndShipmentTenantIdAndActiveTrue(
            @Param("documentId") UUID documentId,
            @Param("tenantId") UUID tenantId);

    @Query("""
        SELECT d FROM Document d
        JOIN FETCH d.shipment s
        WHERE d.id = :documentId
          AND s.tenant.id = :tenantId
          AND s.customer.id = :customerId
          AND d.active = true
    """)
    Optional<Document> findByIdAndShipmentTenantIdAndShipmentCustomerIdAndActiveTrue(
            @Param("documentId") UUID documentId,
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId);

    /**
     * Paginated list of all active documents for a tenant, newest first.
     * Used for the tenant-wide document browser endpoint.
     */
    Page<Document> findByTenantIdAndActiveTrueOrderByUploadedAtDesc(UUID tenantId, Pageable pageable);
}
