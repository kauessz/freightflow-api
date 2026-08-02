package com.freightflow.modules.platform.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeTenantPlanRequest(
        @NotBlank(message = "Plan code is required")
        @Size(max = 50, message = "Plan code must be at most 50 characters")
        String planCode,

        @Size(max = 255, message = "Reason must be at most 255 characters")
        String reason
) {
}
