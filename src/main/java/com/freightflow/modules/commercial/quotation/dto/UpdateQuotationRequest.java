package com.freightflow.modules.commercial.quotation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateQuotationRequest(
        @Size(max = 80) String quotationNumber,
        Instant validUntil,
        @Size(max = 255) String carrierName,
        @Min(0) Integer transitTimeDays,
        @Min(0) Integer freeTimeDays,
        Instant estimatedDeparture,
        Instant estimatedArrival,
        @Size(min = 3, max = 3) String sellingCurrency,
        @Schema(description = "Units of sellingCurrency for 1 unit of costCurrency when conversion is required. Example: USD -> BRL at 5.25 means 100 USD = 525.00 BRL.")
        BigDecimal exchangeRate,
        Instant exchangeRateDate,
        @Size(max = 100) String exchangeRateSource,
        String commercialNotes,
        String internalNotes
) {}
