package com.freightflow.modules.commercial.quotation.dto;

import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;

import java.time.Instant;
import java.util.UUID;

public record QuotationFilterParams(
        String search,
        QuotationStatus status,
        UUID rfqId,
        UUID customerId,
        UUID createdBy,
        Instant validFrom,
        Instant validTo
) {}
