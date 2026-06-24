package com.freightflow.modules.port.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePortRequest(
        @Pattern(regexp = "^[A-Z]{2}[A-Z0-9]{3}$", message = "UNLOCODE must match pattern BRSSZ")
        String unlocode,

        @Size(min = 2, max = 255, message = "Port name must be between 2 and 255 characters")
        String name,

        @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a 2-letter ISO code")
        String country,

        @Size(min = 2, max = 50, message = "Timezone must be between 2 and 50 characters")
        String timezone,

        Double latitude,
        Double longitude,
        Boolean active
) {}
