package com.freightflow.modules.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * Request body for POST /api/v1/webhooks.
 */
public record CreateWebhookRequest(

        @NotBlank(message = "Webhook URL is required")
        @URL(message = "Must be a valid URL (https://...)")
        String url,

        @NotEmpty(message = "At least one event type is required")
        List<@NotBlank(message = "Event type must not be blank") String> events,

        @NotBlank(message = "Secret is required")
        @Size(min = 16, message = "Secret must be at least 16 characters")
        String secret

) {}
