package com.freightflow.modules.document.dto;

import com.freightflow.modules.document.Document;
import com.freightflow.modules.document.DocumentType;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for document endpoints.
 *
 * {@code presignedUrl} is generated per-request with a 1-hour TTL —
 * it must NOT be cached by the client beyond that window.
 */
public record DocumentResponse(
        UUID       id,
        UUID       shipmentId,
        DocumentType type,
        String     fileName,
        String     contentType,
        Long       sizeBytes,
        String     description,
        UUID       uploadedBy,
        Instant    uploadedAt,
        /** Short-lived pre-signed download URL (1 h TTL). */
        String     presignedUrl
) {
    public static DocumentResponse from(Document doc, String presignedUrl) {
        return new DocumentResponse(
                doc.getId(),
                doc.getShipment() != null ? doc.getShipment().getId() : null,
                doc.getType(),
                doc.getFileName(),
                doc.getContentType(),
                doc.getSizeBytes(),
                doc.getDescription(),
                doc.getUploadedBy() != null ? doc.getUploadedBy().getId() : null,
                doc.getUploadedAt(),
                presignedUrl
        );
    }
}
