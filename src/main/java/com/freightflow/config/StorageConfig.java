package com.freightflow.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.net.URISyntaxException;

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
    @Conditional(StorageEndpointConfiguredCondition.class)
    public S3Client s3Client(StorageProperties props) {
        return S3Client.builder()
                .endpointOverride(parseConfiguredEndpoint(props.endpoint()))
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
    @Conditional(StorageEndpointConfiguredCondition.class)
    public S3Presigner s3Presigner(StorageProperties props) {
        return S3Presigner.builder()
                .endpointOverride(parseConfiguredEndpoint(props.endpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.accessKeyId(), props.secretAccessKey())
                ))
                .build();
    }

    static URI parseConfiguredEndpoint(String endpoint) {
        String normalized = endpoint == null ? "" : endpoint.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalStateException(
                    "freightflow.storage.endpoint must be blank to use mock storage or a valid http(s) URL to enable S3-compatible storage."
            );
        }

        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            if (!StringUtils.hasText(scheme) || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new IllegalStateException(
                        "freightflow.storage.endpoint must include http:// or https:// when S3-compatible storage is enabled."
                );
            }
            return uri;
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(
                    "freightflow.storage.endpoint is invalid. Expected a valid http(s) URL for S3-compatible storage.",
                    ex
            );
        }
    }

    static final class StorageEndpointConfiguredCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            return StringUtils.hasText(environment.getProperty("freightflow.storage.endpoint"));
        }
    }
}
