package com.freightflow.modules.document;

import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.document.dto.DocumentResponse;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    /** Maximum accepted file size: 20 MB. */
    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;

    /** Allowed MIME types — maps to pdf, xlsx, csv, jpg, png. */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv",
            "image/jpeg",
            "image/png"
    );

    /** Pre-signed URL validity. */
    private static final Duration PRESIGN_TTL = Duration.ofHours(1);

    private final DocumentRepository documentRepository;
    private final ShipmentRepository shipmentRepository;
    private final TenantRepository   tenantRepository;
    private final UserRepository     userRepository;
    private final StorageService     storageService;

    public DocumentService(DocumentRepository documentRepository,
                           ShipmentRepository shipmentRepository,
                           TenantRepository tenantRepository,
                           UserRepository userRepository,
                           StorageService storageService) {
        this.documentRepository = documentRepository;
        this.shipmentRepository = shipmentRepository;
        this.tenantRepository   = tenantRepository;
        this.userRepository     = userRepository;
        this.storageService     = storageService;
    }

    // ==================== Commands ====================

    /**
     * Validates, uploads and persists a document attached to a shipment.
     *
     * @param tenantId    tenant UUID string (from JWT)
     * @param shipmentId  shipment UUID string (path variable)
     * @param uploadedById user UUID string (from JWT principal)
     * @param type        document type name — one of CTE, BL, NF, OTHER
     * @param description optional free-text description
     * @param file        multipart file from the HTTP request
     * @return {@link DocumentResponse} with a 1-hour pre-signed download URL
     */
    @Transactional
    public DocumentResponse upload(String tenantId, String shipmentId, String uploadedById,
                                   String type, String description, MultipartFile file) {

        // ── Validate file ─────────────────────────────────────────────────
        if (file == null || file.isEmpty()) {
            throw new BusinessException("No file provided");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            long mb = file.getSize() / (1024 * 1024);
            throw new BusinessException("File too large: max 20 MB, received " + mb + " MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(
                    "Unsupported file type: " + contentType +
                    ". Allowed: pdf, xlsx, csv, jpg, png");
        }

        // ── Resolve document type ─────────────────────────────────────────
        DocumentType docType;
        try {
            docType = DocumentType.valueOf(type != null ? type.toUpperCase() : "OTHER");
        } catch (IllegalArgumentException e) {
            docType = DocumentType.OTHER;
        }

        // ── Load referenced entities ──────────────────────────────────────
        UUID tenantUuid   = UUID.fromString(tenantId);
        UUID shipmentUuid = UUID.fromString(shipmentId);

        var tenant = tenantRepository.findById(tenantUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
        var shipment = shipmentRepository.findById(shipmentUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId));

        // ── Upload to storage ─────────────────────────────────────────────
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("Failed to read uploaded file: " + e.getMessage());
        }

        String storageKey = storageService.upload(
                tenantId, shipmentId,
                file.getOriginalFilename(), contentType, bytes);

        log.info("Document uploaded: tenant={} shipment={} key={} type={} size={}B",
                tenantId, shipmentId, storageKey, docType, bytes.length);

        // ── Persist document record ───────────────────────────────────────
        Document document = new Document();
        document.setTenant(tenant);
        document.setShipment(shipment);
        document.setType(docType);
        document.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        document.setStorageKey(storageKey);
        document.setContentType(contentType);
        document.setSizeBytes(file.getSize());
        document.setDescription(description);
        document.setUploadedAt(Instant.now());

        // Uploader is best-effort: JWT principal ID should exist in users table
        if (uploadedById != null) {
            try {
                userRepository.findById(UUID.fromString(uploadedById))
                        .ifPresent(document::setUploadedBy);
            } catch (Exception e) {
                log.debug("Uploader not found in users table: id={}", uploadedById);
            }
        }

        Document saved = documentRepository.save(document);

        // ── Return DTO with fresh presigned URL ───────────────────────────
        String presignedUrl = storageService.generatePresignedUrl(storageKey, PRESIGN_TTL);
        return DocumentResponse.from(saved, presignedUrl);
    }

    // ==================== Queries ====================

    /**
     * Returns all active documents for a shipment, each with a fresh 1-hour presigned URL.
     */
    public List<DocumentResponse> listByShipment(String tenantId, String shipmentId) {
        return documentRepository
                .findByShipmentIdAndActiveTrue(UUID.fromString(shipmentId))
                .stream()
                .map(doc -> {
                    String url = storageService.generatePresignedUrl(doc.getStorageKey(), PRESIGN_TTL);
                    return DocumentResponse.from(doc, url);
                })
                .collect(Collectors.toList());
    }

    // ==================== Delete ====================

    /**
     * Soft-deletes a document record and removes the object from storage.
     *
     * <p>Ownership is enforced: if the document does not belong to {@code tenantId}
     * a 404 is returned (same behaviour as IDOR protection on shipments).</p>
     */
    @Transactional
    public void delete(String tenantId, String documentId) {
        Document doc = documentRepository.findById(UUID.fromString(documentId))
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));

        // Cross-tenant guard
        if (!doc.getTenant().getId().toString().equals(tenantId)) {
            throw new ResourceNotFoundException("Document", documentId);
        }

        // Soft delete — retain record for audit trail
        doc.setActive(false);
        documentRepository.save(doc);
        log.info("Document soft-deleted: id={} key={}", documentId, doc.getStorageKey());

        // Remove from object storage (best-effort — don't fail if storage unavailable)
        try {
            storageService.delete(doc.getStorageKey());
        } catch (Exception e) {
            log.warn("Storage delete failed for key={}: {}", doc.getStorageKey(), e.getMessage());
        }
    }
}
