package com.freightflow.modules.platform.subscription.dto;

import jakarta.validation.constraints.Size;

public record CancelTenantSubscriptionRequest(
        @Size(max = 255, message = "Reason must be at most 255 characters")
        String reason
) {
}
