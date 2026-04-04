package com.freightflow.modules.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Size(min = 2, max = 255) String name,
        @Size(max = 50) String taxId,
        @Size(max = 255) String contactName,
        @Size(max = 255) String contactEmail
) {}
