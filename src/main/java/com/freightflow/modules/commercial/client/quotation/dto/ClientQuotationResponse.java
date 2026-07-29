package com.freightflow.modules.commercial.client.quotation.dto;

import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.shared.IncotermCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ClientQuotationResponse(
        String id,
        String quotationNumber,
        Integer revision,
        QuotationStatus status,
        String rfqId,
        String rfqReference,
        RfqDirection direction,
        RfqTransportMode transportMode,
        RfqServiceType serviceType,
        IncotermCode incotermCode,
        String incotermVersion,
        String incotermNamedPlace,
        String originPortName,
        String originPortUnlocode,
        String destinationPortName,
        String destinationPortUnlocode,
        String carrierName,
        Integer transitTimeDays,
        Integer freeTimeDays,
        Instant estimatedDeparture,
        Instant estimatedArrival,
        String sellingCurrency,
        BigDecimal sellingTotal,
        Instant validUntil,
        String commercialNotes,
        Instant sentAt,
        Instant createdAt,
        Instant updatedAt,
        List<ClientQuotationItemResponse> items
) {
    public static ClientQuotationResponse from(Quotation quotation) {
        RequestForQuotation rfq = quotation.getRfq();
        return new ClientQuotationResponse(
                quotation.getId().toString(),
                quotation.getQuotationNumber(),
                quotation.getRevision(),
                quotation.getStatus(),
                rfq.getId().toString(),
                rfq.getReference(),
                rfq.getDirection(),
                rfq.getTransportMode(),
                rfq.getServiceType(),
                rfq.getIncotermCode(),
                rfq.getIncotermVersion(),
                rfq.getIncotermNamedPlace(),
                rfq.getOriginPort().getName(),
                rfq.getOriginPort().getUnlocode(),
                rfq.getDestinationPort().getName(),
                rfq.getDestinationPort().getUnlocode(),
                quotation.getCarrierName(),
                quotation.getTransitTimeDays(),
                quotation.getFreeTimeDays(),
                quotation.getEstimatedDeparture(),
                quotation.getEstimatedArrival(),
                quotation.getSellingCurrency(),
                quotation.getSellingTotal(),
                quotation.getValidUntil(),
                quotation.getCommercialNotes(),
                quotation.getSentAt(),
                quotation.getCreatedAt(),
                quotation.getUpdatedAt(),
                quotation.getItems().stream()
                        .sorted(java.util.Comparator.comparing(com.freightflow.modules.commercial.quotation.QuotationItem::getSortOrder)
                                .thenComparing(com.freightflow.modules.commercial.quotation.QuotationItem::getCreatedAt))
                        .map(ClientQuotationItemResponse::from)
                        .toList()
        );
    }
}
