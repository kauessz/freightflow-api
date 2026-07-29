package com.freightflow.modules.commercial.client.quotation;

import com.freightflow.modules.commercial.client.quotation.dto.ClientQuotationResponse;
import com.freightflow.modules.commercial.client.quotation.dto.ClientQuotationSummaryResponse;
import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.shared.exception.ForbiddenException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ClientQuotationService {

    private final QuotationRepository quotationRepository;

    public ClientQuotationService(QuotationRepository quotationRepository) {
        this.quotationRepository = quotationRepository;
    }

    public PageResponse<ClientQuotationSummaryResponse> list(UUID tenantId, UUID customerId, Pageable pageable) {
        UUID scopedCustomerId = requireCustomerId(customerId);
        var page = quotationRepository.findByTenantIdAndRfqCustomerIdAndStatus(
                tenantId, scopedCustomerId, QuotationStatus.SENT, pageable
        );
        return PageResponse.from(page.map(ClientQuotationSummaryResponse::from));
    }

    public ClientQuotationResponse getById(UUID id, UUID tenantId, UUID customerId) {
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
