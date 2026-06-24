package com.freightflow.modules.webhook.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * Request body for PUT /api/v1/webhooks/{id}.
 * All fields are optional — only non-null fields are applied.
 */
public record UpdateWebhookRequest(

        @URL(message = "Must be a valid URL (https://...)")
        String url,

        List<String> events,

        @Size(min = 16, message = "Secret must be at least 16 characters")
        String secret,

        Boolean active

) {}
