package com.freightflow.modules.platform.catalog.dto;

import com.freightflow.modules.platform.catalog.SubscriptionPlanStatus;

import java.util.UUID;

public record SubscriptionPlanSummaryResponse(
        UUID id,
        String code,
        String name,
        String description,
        SubscriptionPlanStatus status,
        int displayOrder,
        boolean custom
) {
}
