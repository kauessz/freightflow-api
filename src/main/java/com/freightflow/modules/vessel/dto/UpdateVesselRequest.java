package com.freightflow.modules.vessel.dto;

import com.freightflow.modules.vessel.enums.VesselType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateVesselRequest(
    @Pattern(regexp = "^$|^\\d{7}$", message = "IMO number must be exactly 7 digits when provided")
    String imo,

    @Size(min = 2, max = 200, message = "Vessel name must be between 2 and 200 characters")
    String name,

    @Size(min = 2, max = 2, message = "Flag must be a 2-letter ISO country code")
    String flag,

    VesselType type,

    @Positive(message = "Capacity TEU must be positive")
    Integer capacityTeu,

    @Size(max = 100, message = "Carrier must be at most 100 characters")
    String carrier,

    Boolean active
) {}
