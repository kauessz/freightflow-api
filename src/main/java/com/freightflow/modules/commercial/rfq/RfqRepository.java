package com.freightflow.modules.commercial.rfq;

import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RfqRepository extends JpaRepository<RequestForQuotation, UUID>, JpaSpecificationExecutor<RequestForQuotation> {

    Optional<RequestForQuotation> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByReferenceAndTenantId(String reference, UUID tenantId);

    boolean existsByReferenceAndTenantIdAndIdNot(String reference, UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, RfqStatus status);

    Page<RequestForQuotation> findByTenantId(UUID tenantId, Pageable pageable);

    List<RequestForQuotation> findByTenantIdAndStatus(UUID tenantId, RfqStatus status);
}
