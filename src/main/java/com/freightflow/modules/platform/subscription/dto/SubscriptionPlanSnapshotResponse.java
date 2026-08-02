package com.freightflow.modules.platform.subscription.dto;

import com.freightflow.modules.platform.catalog.SubscriptionPlanStatus;

import java.util.UUID;

public record SubscriptionPlanSnapshotResponse(
        UUID id,
        String code,
        String name,
        SubscriptionPlanStatus status
) {
}
