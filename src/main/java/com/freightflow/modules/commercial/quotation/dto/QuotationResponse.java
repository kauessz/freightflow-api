package com.freightflow.modules.commercial.quotation.dto;

import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.dto.RfqSummaryResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record QuotationResponse(
        String id,
        String quotationNumber,
        Integer revision,
        QuotationStatus status,
        RfqSummaryResponse rfq,
        String carrierName,
        Integer transitTimeDays,
        Integer freeTimeDays,
        Instant estimatedDeparture,
        Instant estimatedArrival,
        String sellingCurrency,
        BigDecimal exchangeRate,
        Instant exchangeRateDate,
        String exchangeRateSource,
        BigDecimal costTotal,
        BigDecimal sellingTotal,
        BigDecimal profitAmount,
        BigDecimal marginPercentage,
        BigDecimal markupPercentage,
        Instant validUntil,
        String commercialNotes,
        String internalNotes,
        String createdById,
        String createdByName,
        Instant submittedAt,
        Instant approvedAt,
        Instant rejectedAt,
        Instant expiredAt,
        Instant createdAt,
        Instant updatedAt,
        List<QuotationItemResponse> items
) {
    public static QuotationResponse from(Quotation quotation, long rfqQuotationCount) {
        return new QuotationResponse(
                quotation.getId().toString(),
                quotation.getQuotationNumber(),
                quotation.getRevision(),
                quotation.getStatus(),
                RfqSummaryResponse.from(quotation.getRfq(), rfqQuotationCount),
                quotation.getCarrierName(),
                quotation.getTransitTimeDays(),
                quotation.getFreeTimeDays(),
                quotation.getEstimatedDeparture(),
                quotation.getEstimatedArrival(),
                quotation.getSellingCurrency(),
                quotation.getExchangeRate(),
                quotation.getExchangeRateDate(),
                quotation.getExchangeRateSource(),
                quotation.getCostTotal(),
                quotation.getSellingTotal(),
                quotation.getProfitAmount(),
                quotation.getMarginPercentage(),
                quotation.getMarkupPercentage(),
                quotation.getValidUntil(),
                quotation.getCommercialNotes(),
                quotation.getInternalNotes(),
                quotation.getCreatedBy().getId().toString(),
                quotation.getCreatedBy().getName(),
                quotation.getSubmittedAt(),
                quotation.getApprovedAt(),
                quotation.getRejectedAt(),
                quotation.getExpiredAt(),
                quotation.getCreatedAt(),
                quotation.getUpdatedAt(),
                quotation.getItems().stream().map(QuotationItemResponse::from).toList()
        );
    }
}
