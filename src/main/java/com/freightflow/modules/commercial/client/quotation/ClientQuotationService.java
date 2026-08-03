package com.freightflow.modules.commercial.client.quotation;

import com.freightflow.modules.commercial.client.quotation.dto.ClientQuotationResponse;
import com.freightflow.modules.commercial.client.quotation.dto.ClientQuotationSummaryResponse;
import com.freightflow.modules.platform.entitlement.EntitlementEnforcementService;
import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.shared.exception.ForbiddenException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ClientQuotationService {

    private static final String CLIENT_PORTAL_FEATURE_KEY = "CLIENT_PORTAL";
    private static final String QUOTATION_WORKFLOW_FEATURE_KEY = "QUOTATION_WORKFLOW";

    private final QuotationRepository quotationRepository;
    private final EntitlementEnforcementService entitlementEnforcementService;

    public ClientQuotationService(QuotationRepository quotationRepository,
                                  EntitlementEnforcementService entitlementEnforcementService) {
        this.quotationRepository = quotationRepository;
        this.entitlementEnforcementService = entitlementEnforcementService;
    }

    public PageResponse<ClientQuotationSummaryResponse> list(UUID tenantId, UUID customerId, Pageable pageable) {
        entitlementEnforcementService.requireAllEnabled(
                tenantId,
                List.of(CLIENT_PORTAL_FEATURE_KEY, QUOTATION_WORKFLOW_FEATURE_KEY)
        );
        UUID scopedCustomerId = requireCustomerId(customerId);
        var page = quotationRepository.findByTenantIdAndRfqCustomerIdAndStatus(
                tenantId, scopedCustomerId, QuotationStatus.SENT, pageable
        );
        return PageResponse.from(page.map(ClientQuotationSummaryResponse::from));
    }

    public ClientQuotationResponse getById(UUID id, UUID tenantId, UUID customerId) {
        entitlementEnforcementService.requireAllEnabled(
                tenantId,
                List.of(CLIENT_PORTAL_FEATURE_KEY, QUOTATION_WORKFLOW_FEATURE_KEY)
        );
        UUID scopedCustomerId = requireCustomerId(customerId);
        Quotation quotation = quotationRepository.findByIdAndTenantIdAndRfqCustomerIdAndStatus(
                id, tenantId, scopedCustomerId, QuotationStatus.SENT
        ).orElseThrow(() -> new ResourceNotFoundException("Quotation", id));
        return ClientQuotationResponse.from(quotation);
    }

    private UUID requireCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new ForbiddenException("Client user is not linked to a customer");
        }
        return customerId;
    }
}
