package com.freightflow.modules.commercial.rfq.dto;

import com.freightflow.modules.commercial.rfq.enums.RfqContainerType;
import com.freightflow.modules.commercial.shared.WeightUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RfqContainerRequest(
        @NotNull RfqContainerType containerType,
        @NotNull @Min(1) Integer quantity,
        @DecimalMin(value = "0.001") BigDecimal weightPerContainer,
        WeightUnit weightUnit,
        String notes
) {}
