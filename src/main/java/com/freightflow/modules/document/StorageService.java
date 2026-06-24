package com.freightflow.modules.document;

import com.freightflow.config.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Abstraction over Cloudflare R2 (S3-compatible) for document storage.
 *
 * <h3>Modes</h3>
 * <ul>
 *   <li><b>R2 mode</b> — when {@code freightflow.storage.endpoint} is set, both
 *       {@link S3Client} and {@link S3Presigner} beans are present. Files are
 *       stored in Cloudflare R2 and presigned download URLs are generated via AWS SDK.</li>
 *   <li><b>Mock mode</b> — when the endpoint is blank (local dev), neither bean is
 *       created and this service writes files to {@code /tmp} and returns a fake URL.
 *       The application starts and functions normally without any R2 credentials.</li>
 * </ul>
 *
 * <p>Both {@link S3Client} and {@link S3Presigner} are injected with
 * {@code required = false} so Spring does not fail when the beans are absent.</p>
 */
@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final StorageProperties storageProperties;

    /** Null when {@code freightflow.storage.endpoint} is blank (mock mode). */
    @Autowired(required = false)
    private S3Client s3Client;

    /** Null when {@code freightflow.storage.endpoint} is blank (mock mode). */
    @Autowired(required = false)
    private S3Presigner s3Presigner;

    public StorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    /**
     * Uploads {@code content} to R2 (or /tmp in mock mode).
     *
     * @param tenantId    tenant UUID string — used as first path segment of the key
     * @param shipmentId  shipment UUID string — used as second path segment
     * @param fileName    original file name as provided by the uploader
     * @param contentType MIME type (e.g. {@code application/pdf})
     * @param content     raw file bytes
     * @return the storage key, e.g. {@code {tenantId}/{shipmentId}/{uuid}-{fileName}}
     */
    public String upload(String tenantId, String shipmentId,
                         String fileName, String contentType, byte[] content) {
        String storageKey = buildKey(tenantId, shipmentId, fileName);

        if (s3Client != null) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(storageProperties.bucket())
                    .key(storageKey)
                    .contentType(contentType)
                    .contentLength((long) content.length)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(content));
            log.info("Uploaded to R2: bucket={} key={} size={}B",
                    storageProperties.bucket(), storageKey, content.length);
        } else {
            // Mock mode: write to /tmp for local inspection
            try {
                String safeName = storageKey.replace("/", "_");
                Path target = Path.of("/tmp", safeName);
                Files.write(target, content);
                log.info("Mock storage: written to {}", target);
            } catch (IOException e) {
                log.warn("Mock storage write failed for key={}: {}", storageKey, e.getMessage());
            }
        }

        return storageKey;
    }

    /**
     * Generates a short-lived pre-signed GET URL for the given storage key.
     *
     * <p>In mock mode, returns a fake URL containing the expiry timestamp so the
     * caller can still exercise the full code path during development.</p>
     *
     * @param storageKey the key returned by {@link #upload}
     * @param expiry     URL validity duration (typically 1 hour)
     * @return pre-signed URL string
     */
    public String generatePresignedUrl(String storageKey, Duration expiry) {
        if (s3Presigner != null) {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .getObjectRequest(r -> r
                            .bucket(storageProperties.bucket())
                            .key(storageKey))
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        }

        // Mock mode: build a fake URL from publicBaseUrl (or localhost fallback)
        String base = (storageProperties.publicBaseUrl() != null
                && !storageProperties.publicBaseUrl().isBlank())
                ? storageProperties.publicBaseUrl()
                : "http://localhost:8080/mock-storage";
        long expiresAt = Instant.now().plus(expiry).getEpochSecond();
        return base + "/" + storageKey + "?expires=" + expiresAt;
    }

    /**
     * Deletes the object from R2. In mock mode this is a no-op.
     *
     * @param storageKey the key returned by {@link #upload}
     */
    public void delete(String storageKey) {
        if (s3Client != null) {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(storageProperties.bucket())
                    .key(storageKey)
                    .build();
            s3Client.deleteObject(request);
            log.info("Deleted from R2: key={}", storageKey);
        } else {
            log.debug("Mock storage: skipping delete for key={}", storageKey);
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    /**
     * Builds the object key: {@code {tenantId}/{shipmentId}/{uuid}-{sanitizedFileName}}.
     * A UUID prefix prevents name collisions when the same file is uploaded twice.
     */
    private String buildKey(String tenantId, String shipmentId, String fileName) {
        String sanitized = (fileName != null ? fileName : "file")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return tenantId + "/" + shipmentId + "/" + UUID.randomUUID() + "-" + sanitized;
    }
}
