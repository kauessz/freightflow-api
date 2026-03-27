package com.freightflow.modules.event.dto;

import com.freightflow.modules.event.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateEventRequest(
    @NotNull(message = "Event type is required")
    EventType type,

    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must be at most 200 characters")
    String location,

    @Size(max = 500, message = "Description must be at most 500 characters")
    String description,

    @NotNull(message = "Occurred at timestamp is required")
    Instant occurredAt
) {}
