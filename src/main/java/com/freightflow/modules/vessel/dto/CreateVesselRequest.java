package com.freightflow.modules.vessel.dto;

import com.freightflow.modules.vessel.enums.VesselType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateVesselRequest(
    @NotBlank(message = "IMO number is required")
    @Pattern(regexp = "^\\d{7}$", message = "IMO number must be exactly 7 digits")
    String imo,

    @NotBlank(message = "Vessel name is required")
    @Size(min = 2, max = 200, message = "Vessel name must be between 2 and 200 characters")
    String name,

    @NotBlank(message = "Flag is required")
    @Size(min = 2, max = 2, message = "Flag must be a 2-letter ISO country code")
    String flag,

    @NotNull(message = "Vessel type is required")
    VesselType type,

    @NotNull(message = "Capacity TEU is required")
    @Positive(message = "Capacity TEU must be positive")
    Integer capacityTeu
) {}
