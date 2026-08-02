package com.freightflow.modules.platform.subscription.dto;

import java.util.UUID;

public record TenantSubscriptionCurrentResponse(
        UUID tenantId,
        TenantSubscriptionResponse currentSubscription
) {
}
