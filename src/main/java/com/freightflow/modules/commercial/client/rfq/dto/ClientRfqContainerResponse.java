package com.freightflow.modules.commercial.client.rfq.dto;

import com.freightflow.modules.commercial.rfq.RfqContainerRequirement;
import com.freightflow.modules.commercial.rfq.enums.RfqContainerType;
import com.freightflow.modules.commercial.shared.WeightUnit;

import java.math.BigDecimal;

public record ClientRfqContainerResponse(
        String id,
        RfqContainerType containerType,
        Integer quantity,
        BigDecimal weightPerContainer,
        WeightUnit weightUnit,
        String notes
) {
    public static ClientRfqContainerResponse from(RfqContainerRequirement item) {
        return new ClientRfqContainerResponse(
                item.getId().toString(),
                item.getContainerType(),
                item.getQuantity(),
                item.getWeightPerContainer(),
                item.getWeightUnit(),
                item.getNotes()
        );
    }
}
