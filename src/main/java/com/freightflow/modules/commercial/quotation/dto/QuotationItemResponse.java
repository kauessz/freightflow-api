package com.freightflow.modules.commercial.quotation.dto;

import com.freightflow.modules.commercial.quotation.QuotationItem;
import com.freightflow.modules.commercial.quotation.enums.ChargeCategory;
import com.freightflow.modules.commercial.quotation.enums.ChargeScope;

import java.math.BigDecimal;
import java.time.Instant;

public record QuotationItemResponse(
        String id,
        ChargeCategory category,
        String description,
        ChargeScope scope,
        String costCurrency,
        BigDecimal costAmount,
        BigDecimal exchangeRate,
        BigDecimal costAmountInSellingCurrency,
        String sellingCurrency,
        BigDecimal sellingAmount,
        BigDecimal quantity,
        String unit,
        BigDecimal totalCost,
        BigDecimal totalSelling,
        BigDecimal profitAmount,
        BigDecimal marginPercentage,
        BigDecimal markupPercentage,
        boolean included,
        boolean optional,
        String supplierName,
        String notes,
        Integer sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
    public static QuotationItemResponse from(QuotationItem item) {
        return new QuotationItemResponse(
                item.getId().toString(),
                item.getCategory(),
                item.getDescription(),
                item.getScope(),
                item.getCostCurrency(),
                item.getCostAmount(),
                item.getExchangeRate(),
                item.getCostAmountInSellingCurrency(),
                item.getSellingCurrency(),
                item.getSellingAmount(),
                item.getQuantity(),
                item.getUnit(),
                item.getTotalCost(),
                item.getTotalSelling(),
                item.getProfitAmount(),
                item.getMarginPercentage(),
                item.getMarkupPercentage(),
                item.isIncluded(),
                item.isOptional(),
                item.getSupplierName(),
                item.getNotes(),
                item.getSortOrder(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
