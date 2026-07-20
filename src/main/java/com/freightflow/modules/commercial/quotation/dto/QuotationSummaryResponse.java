package com.freightflow.modules.commercial.quotation.dto;

import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record QuotationSummaryResponse(
        String id,
        String quotationNumber,
        Integer revision,
        QuotationStatus status,
        String rfqId,
        String rfqReference,
        String customerId,
        String customerName,
        String sellingCurrency,
        BigDecimal costTotal,
        BigDecimal sellingTotal,
        BigDecimal profitAmount,
        BigDecimal marginPercentage,
        BigDecimal markupPercentage,
        Instant validUntil,
        Instant createdAt,
        Instant updatedAt
) {
    public static QuotationSummaryResponse from(Quotation quotation) {
        return new QuotationSummaryResponse(
                quotation.getId().toString(),
                quotation.getQuotationNumber(),
                quotation.getRevision(),
                quotation.getStatus(),
                quotation.getRfq().getId().toString(),
                quotation.getRfq().getReference(),
                quotation.getRfq().getCustomer() != null ? quotation.getRfq().getCustomer().getId().toString() : null,
                quotation.getRfq().getCustomer() != null ? quotation.getRfq().getCustomer().getName() : quotation.getRfq().getProspectCompanyName(),
                quotation.getSellingCurrency(),
                quotation.getCostTotal(),
                quotation.getSellingTotal(),
                quotation.getProfitAmount(),
                quotation.getMarginPercentage(),
                quotation.getMarkupPercentage(),
                quotation.getValidUntil(),
                quotation.getCreatedAt(),
                quotation.getUpdatedAt()
        );
    }
}
