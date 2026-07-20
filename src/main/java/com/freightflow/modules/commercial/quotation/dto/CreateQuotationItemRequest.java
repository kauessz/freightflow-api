package com.freightflow.modules.commercial.quotation.dto;

import com.freightflow.modules.commercial.quotation.enums.ChargeCategory;
import com.freightflow.modules.commercial.quotation.enums.ChargeScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateQuotationItemRequest(
        @NotNull ChargeCategory category,
        @NotBlank @Size(max = 500) String description,
        @NotNull ChargeScope scope,
        @NotBlank @Size(min = 3, max = 3) String costCurrency,
        @NotNull @DecimalMin(value = "0.00") BigDecimal costAmount,
        @Schema(description = "Units of sellingCurrency for 1 unit of costCurrency. Example: 100 USD with exchangeRate 5.25 into BRL results in 525.00 BRL.")
        BigDecimal exchangeRate,
        @NotBlank @Size(min = 3, max = 3) String sellingCurrency,
        @NotNull @DecimalMin(value = "0.00") BigDecimal sellingAmount,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantity,
        @Size(max = 50) String unit,
        Boolean included,
        Boolean optional,
        @Size(max = 255) String supplierName,
        String notes,
        @Min(0) Integer sortOrder
) {}
