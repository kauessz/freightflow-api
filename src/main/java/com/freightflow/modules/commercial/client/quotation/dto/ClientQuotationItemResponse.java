package com.freightflow.modules.commercial.client.quotation.dto;

import com.freightflow.modules.commercial.quotation.QuotationItem;
import com.freightflow.modules.commercial.quotation.enums.ChargeCategory;
import com.freightflow.modules.commercial.quotation.enums.ChargeScope;

import java.math.BigDecimal;

public record ClientQuotationItemResponse(
        String id,
        ChargeCategory category,
        String description,
        ChargeScope scope,
        String sellingCurrency,
        BigDecimal sellingAmount,
        BigDecimal quantity,
        String unit,
        BigDecimal totalSelling,
        boolean included,
        boolean optional,
        String notes,
        Integer sortOrder
) {
    public static ClientQuotationItemResponse from(QuotationItem item) {
        return new ClientQuotationItemResponse(
                item.getId().toString(),
                item.getCategory(),
                item.getDescription(),
                item.getScope(),
                item.getSellingCurrency(),
                item.getSellingAmount(),
                item.getQuantity(),
                item.getUnit(),
                item.getTotalSelling(),
                item.isIncluded(),
                item.isOptional(),
                item.getNotes(),
                item.getSortOrder()
        );
    }
}
