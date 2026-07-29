package com.freightflow.modules.commercial.client.rfq;

import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqCreateRequest;
import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqResponse;
import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqSummaryResponse;
import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqUpdateRequest;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.RfqRepository;
import com.freightflow.modules.commercial.rfq.RfqService;
import com.freightflow.modules.commercial.rfq.dto.CreateRfqRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqFilterParams;
import com.freightflow.modules.commercial.rfq.dto.UpdateRfqRequest;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ForbiddenException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ClientRfqService {

    private final RfqService rfqService;
    private final RfqRepository rfqRepository;
    private final QuotationRepository quotationRepository;

    public ClientRfqService(RfqService rfqService,
                            RfqRepository rfqRepository,
                            QuotationRepository quotationRepository) {
        this.rfqService = rfqService;
        this.rfqRepository = rfqRepository;
        this.quotationRepository = quotationRepository;
    }

    @Transactional
    public ClientRfqResponse create(ClientRfqCreateRequest request, UUID tenantId, UUID customerId, UUID userId) {
        UUID scopedCustomerId = requireCustomerId(customerId);
        enforceOceanOnly(request.transportMode());

        var response = rfqService.create(new CreateRfqRequest(
                request.reference(),
                scopedCustomerId,
                null,
                request.contactName(),
                request.contactEmail(),
                request.contactPhone(),
                request.direction(),
                request.transportMode(),
                request.serviceType(),
                request.incotermCode(),
                request.incotermVersion(),
                request.incotermNamedPlace(),
                request.originPortId(),
                request.destinationPortId(),
                request.placeOfReceipt(),
                request.placeOfDelivery(),
                request.cargoReadyDate(),
                request.desiredDepartureDate(),
                null,
                request.notes(),
                request.cargoItems(),
                request.containers()
        ), tenantId, userId);

        RequestForQuotation rfq = getScopedRfq(UUID.fromString(response.id()), tenantId, scopedCustomerId);
        return mapDetail(rfq, tenantId);
    }

    public PageResponse<ClientRfqSummaryResponse> list(UUID tenantId, UUID customerId, Pageable pageable) {
        UUID scopedCustomerId = requireCustomerId(customerId);
        var page = rfqRepository.findByTenantIdAndCustomerId(tenantId, scopedCustomerId, pageable);
        Map<UUID, Long> quotationCounts = countQuotations(
                tenantId,
                page.getContent().stream().map(RequestForQuotation::getId).toList()
        );
        return PageResponse.from(page.map(rfq -> ClientRfqSummaryResponse.from(
                rfq, quotationCounts.getOrDefault(rfq.getId(), 0L)
        )));
    }

    public ClientRfqResponse getById(UUID id, UUID tenantId, UUID customerId) {
        UUID scopedCustomerId = requireCustomerId(customerId);
        return mapDetail(getScopedRfq(id, tenantId, scopedCustomerId), tenantId);
    }

    @Transactional
    public ClientRfqResponse update(UUID id, ClientRfqUpdateRequest request, UUID tenantId, UUID customerId) {
        UUID scopedCustomerId = requireCustomerId(customerId);
        RequestForQuotation rfq = getScopedRfq(id, tenantId, scopedCustomerId);
        if (rfq.getStatus() != RfqStatus.DRAFT) {
            throw new BusinessException("Only RFQs in DRAFT can be structurally updated");
        }
        if (request.transportMode() != null) {
            enforceOceanOnly(request.transportMode());
        }

        rfqService.update(id, new UpdateRfqRequest(
                request.reference(),
                null,
                null,
                request.contactName(),
                request.contactEmail(),
                request.contactPhone(),
                request.direction(),
                request.transportMode(),
                request.serviceType(),
                request.incotermCode(),
                request.incotermVersion(),
                request.incotermNamedPlace(),
                request.originPortId(),
                request.destinationPortId(),
                request.placeOfReceipt(),
                request.placeOfDelivery(),
                request.cargoReadyDate(),
                request.desiredDepartureDate(),
                null,
                request.notes(),
                request.cargoItems(),
                request.containers()
        ), tenantId);

        return mapDetail(getScopedRfq(id, tenantId, scopedCustomerId), tenantId);
    }

    @Transactional
    public ClientRfqResponse submit(UUID id, UUID tenantId, UUID customerId) {
        UUID scopedCustomerId = requireCustomerId(customerId);
        RequestForQuotation rfq = getScopedRfq(id, tenantId, scopedCustomerId);
        if (rfq.getStatus() != RfqStatus.DRAFT) {
            throw new BusinessException("Only RFQs in DRAFT can be submitted");
        }

        rfqService.submit(id, tenantId);
        return mapDetail(getScopedRfq(id, tenantId, scopedCustomerId), tenantId);
    }

    @Transactional
    public ClientRfqResponse cancel(UUID id, UUID tenantId, UUID customerId) {
        UUID scopedCustomerId = requireCustomerId(customerId);
        RequestForQuotation rfq = getScopedRfq(id, tenantId, scopedCustomerId);
        if (rfq.getStatus() != RfqStatus.DRAFT && rfq.getStatus() != RfqStatus.SUBMITTED) {
            throw new BusinessException("Client can only cancel RFQs in DRAFT or SUBMITTED");
        }

        rfqService.cancel(id, tenantId);
        return mapDetail(getScopedRfq(id, tenantId, scopedCustomerId), tenantId);
    }

    private ClientRfqResponse mapDetail(RequestForQuotation rfq, UUID tenantId) {
        long quotationCount = countQuotations(tenantId, List.of(rfq.getId()))
                .getOrDefault(rfq.getId(), 0L);
        return ClientRfqResponse.from(rfq, quotationCount);
    }

    private RequestForQuotation getScopedRfq(UUID id, UUID tenantId, UUID customerId) {
        return rfqRepository.findByIdAndTenantIdAndCustomerId(id, tenantId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("RequestForQuotation", id));
    }

    private UUID requireCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new ForbiddenException("Client user is not linked to a customer");
        }
        return customerId;
    }

    private void enforceOceanOnly(RfqTransportMode transportMode) {
        if (transportMode != RfqTransportMode.OCEAN) {
            throw new BusinessException("Client portal currently supports only OCEAN RFQs");
        }
    }

    private Map<UUID, Long> countQuotations(UUID tenantId, List<UUID> rfqIds) {
        if (rfqIds.isEmpty()) {
            return Map.of();
        }
        return quotationRepository.countByRfqIdsAndTenantId(rfqIds, tenantId).stream()
                .collect(Collectors.toMap(
                        QuotationRepository.RfqQuotationCountView::getRfqId,
                        QuotationRepository.RfqQuotationCountView::getQuotationCount
                ));
    }
}
