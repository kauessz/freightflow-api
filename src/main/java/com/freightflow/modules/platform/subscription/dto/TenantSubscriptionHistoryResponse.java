package com.freightflow.modules.platform.subscription.dto;

import java.util.List;
import java.util.UUID;

public record TenantSubscriptionHistoryResponse(
        UUID tenantId,
        List<TenantSubscriptionResponse> subscriptions,
        List<TenantSubscriptionEventResponse> events
) {
}
