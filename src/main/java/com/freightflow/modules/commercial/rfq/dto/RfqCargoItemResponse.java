package com.freightflow.modules.commercial.rfq.dto;

import com.freightflow.modules.commercial.rfq.RfqCargoItem;
import com.freightflow.modules.commercial.shared.VolumeUnit;
import com.freightflow.modules.commercial.shared.WeightUnit;

import java.math.BigDecimal;
import java.time.Instant;

public record RfqCargoItemResponse(
        String id,
        String description,
        String packageType,
        Integer packageQuantity,
        BigDecimal grossWeight,
        WeightUnit weightUnit,
        BigDecimal volume,
        VolumeUnit volumeUnit,
        String hsCode,
        boolean dangerousGoods,
        String unNumber,
        boolean temperatureControlled,
        BigDecimal minimumTemperature,
        BigDecimal maximumTemperature,
        Boolean stackable,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    public static RfqCargoItemResponse from(RfqCargoItem item) {
        return new RfqCargoItemResponse(
                item.getId().toString(),
                item.getDescription(),
                item.getPackageType(),
                item.getPackageQuantity(),
                item.getGrossWeight(),
                item.getWeightUnit(),
                item.getVolume(),
                item.getVolumeUnit(),
                item.getHsCode(),
                item.isDangerousGoods(),
                item.getUnNumber(),
                item.isTemperatureControlled(),
                item.getMinimumTemperature(),
                item.getMaximumTemperature(),
                item.getStackable(),
                item.getNotes(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
