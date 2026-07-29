package com.freightflow.modules.commercial.quotation;

import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuotationRepository extends JpaRepository<Quotation, UUID>, JpaSpecificationExecutor<Quotation> {

    interface RfqQuotationCountView {
        UUID getRfqId();
        long getQuotationCount();
    }

    Optional<Quotation> findByIdAndTenantId(UUID id, UUID tenantId);

    @EntityGraph(attributePaths = {
            "rfq", "rfq.customer", "rfq.originPort", "rfq.destinationPort",
            "items", "createdBy"
    })
    Optional<Quotation> findByIdAndTenantIdAndRfqCustomerIdAndStatus(
            UUID id, UUID tenantId, UUID customerId, QuotationStatus status);

    boolean existsByTenantIdAndQuotationNumberAndRevision(UUID tenantId, String quotationNumber, Integer revision);

    boolean existsByTenantIdAndQuotationNumberAndRevisionAndIdNot(UUID tenantId, String quotationNumber, Integer revision, UUID id);

    Page<Quotation> findByTenantId(UUID tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"rfq", "rfq.customer", "rfq.originPort", "rfq.destinationPort"})
    Page<Quotation> findByTenantIdAndRfqCustomerIdAndStatus(
            UUID tenantId, UUID customerId, QuotationStatus status, Pageable pageable);

    @Query("""
        SELECT q.rfq.id AS rfqId, COUNT(q) AS quotationCount
        FROM Quotation q
        WHERE q.rfq.id IN :rfqIds
          AND q.tenant.id = :tenantId
        GROUP BY q.rfq.id
    """)
    List<RfqQuotationCountView> countByRfqIdsAndTenantId(
            @Param("rfqIds") List<UUID> rfqIds,
            @Param("tenantId") UUID tenantId
    );

    long countByTenantIdAndStatus(UUID tenantId, QuotationStatus status);
}
