package com.freightflow.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Binding for {@code freightflow.storage.*} properties.
 *
 * <p>Endpoint and credentials are optional (env vars default to empty string) —
 * when blank, {@link StorageConfig} does NOT create the S3Client bean and
 * {@link com.freightflow.modules.document.StorageService} runs in mock mode.</p>
 */
@ConfigurationProperties("freightflow.storage")
@Validated
public record StorageProperties(
        String endpoint,
        String bucket,
        String accessKeyId,
        String secretAccessKey,
        String publicBaseUrl
) {}
