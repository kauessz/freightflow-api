package com.freightflow.modules.commercial.rfq.dto;

import com.freightflow.modules.commercial.rfq.RfqContainerRequirement;
import com.freightflow.modules.commercial.rfq.enums.RfqContainerType;
import com.freightflow.modules.commercial.shared.WeightUnit;

import java.math.BigDecimal;
import java.time.Instant;

public record RfqContainerResponse(
        String id,
        RfqContainerType containerType,
        Integer quantity,
        BigDecimal weightPerContainer,
        WeightUnit weightUnit,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    public static RfqContainerResponse from(RfqContainerRequirement item) {
        return new RfqContainerResponse(
                item.getId().toString(),
                item.getContainerType(),
                item.getQuantity(),
                item.getWeightPerContainer(),
                item.getWeightUnit(),
                item.getNotes(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
