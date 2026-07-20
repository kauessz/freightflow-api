package com.freightflow.modules.commercial.rfq.dto;

import com.freightflow.modules.commercial.shared.VolumeUnit;
import com.freightflow.modules.commercial.shared.WeightUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RfqCargoItemRequest(
        @NotBlank @Size(max = 500) String description,
        @Size(max = 100) String packageType,
        @NotNull @Min(1) Integer packageQuantity,
        @NotNull @DecimalMin(value = "0.001") BigDecimal grossWeight,
        @NotNull WeightUnit weightUnit,
        @DecimalMin(value = "0.001") BigDecimal volume,
        VolumeUnit volumeUnit,
        @Size(max = 50) String hsCode,
        boolean dangerousGoods,
        @Size(max = 20) String unNumber,
        boolean temperatureControlled,
        BigDecimal minimumTemperature,
        BigDecimal maximumTemperature,
        Boolean stackable,
        String notes
) {}
