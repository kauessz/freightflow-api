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

public record ClientQuotationSummaryResponse(
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
        String carrierName,
        Integer transitTimeDays,
        Integer freeTimeDays,
        String sellingCurrency,
        BigDecimal sellingTotal,
        Instant validUntil,
        Instant sentAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static ClientQuotationSummaryResponse from(Quotation quotation) {
        RequestForQuotation rfq = quotation.getRfq();
        return new ClientQuotationSummaryResponse(
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
                quotation.getCarrierName(),
                quotation.getTransitTimeDays(),
                quotation.getFreeTimeDays(),
                quotation.getSellingCurrency(),
                quotation.getSellingTotal(),
                quotation.getValidUntil(),
                quotation.getSentAt(),
                quotation.getCreatedAt(),
                quotation.getUpdatedAt()
        );
    }
}
