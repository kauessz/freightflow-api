package com.freightflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Wires up the AWS SDK S3Client and S3Presigner pointing at Cloudflare R2.
 *
 * <p>Both beans are conditional on {@code freightflow.storage.endpoint} being
 * present and non-empty. When the endpoint is blank (local dev without R2),
 * no bean is created and {@link com.freightflow.modules.document.StorageService}
 * falls back to mock mode (files written to /tmp).</p>
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    /**
     * S3-compatible client for upload and delete operations against Cloudflare R2.
     * Path-style access is required because R2 does not support virtual-hosted buckets.
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "freightflow.storage",
            name = "endpoint",
            matchIfMissing = false
    )
    public S3Client s3Client(StorageProperties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.accessKeyId(), props.secretAccessKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    /**
     * Pre-signer used to generate short-lived download URLs for stored documents.
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "freightflow.storage",
            name = "endpoint",
            matchIfMissing = false
    )
    public S3Presigner s3Presigner(StorageProperties props) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.accessKeyId(), props.secretAccessKey())
                ))
                .build();
    }
}
