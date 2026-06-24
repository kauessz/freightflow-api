package com.freightflow.modules.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /**
     * Returns all active documents linked to a shipment, ordered by the default
     * entity order (uploaded_at DESC via the SQL index direction).
     */
    List<Document> findByShipmentIdAndActiveTrue(UUID shipmentId);

    /**
     * Paginated list of all active documents for a tenant, newest first.
     * Used for the tenant-wide document browser endpoint.
     */
    Page<Document> findByTenantIdAndActiveTrueOrderByUploadedAtDesc(UUID tenantId, Pageable pageable);
}
