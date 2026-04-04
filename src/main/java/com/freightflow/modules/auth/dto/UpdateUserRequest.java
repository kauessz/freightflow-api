package com.freightflow.modules.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 255) String name,
        @Pattern(regexp = "ADMIN|OPERATOR|VIEWER|CLIENT") String role,
        String customerId,
        Boolean active
) {}
