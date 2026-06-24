package com.freightflow.modules.document;

import com.freightflow.modules.document.dto.DocumentResponse;
import com.freightflow.shared.rbac.RequiresRole;
import com.freightflow.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints for document management.
 *
 * <ul>
 *   <li>POST   /api/v1/shipments/{id}/documents  — upload a file (multipart)</li>
 *   <li>GET    /api/v1/shipments/{id}/documents  — list documents for a shipment</li>
 *   <li>DELETE /api/v1/documents/{id}            — soft-delete a document</li>
 * </ul>
 */
@RestController
@Tag(name = "Documents", description = "Document storage and retrieval — backed by Cloudflare R2")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // ── Upload ──────────────────────────────────────────────────────────────

    @PostMapping(
            value = "/api/v1/shipments/{id}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(
            summary = "Upload a document to a shipment",
            description = """
                Accepts multipart/form-data. Fields:
                - file: the file binary (required) — max 20 MB
                - type: document type — CTE, BL, NF or OTHER (required)
                - description: optional note (max 500 chars)

                Allowed MIME types: application/pdf, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,
                text/csv, image/jpeg, image/png.

                Returns 201 with a DocumentResponse including a 1-hour presigned download URL.
                """
    )
    public ResponseEntity<DocumentResponse> upload(
            @PathVariable("id") UUID shipmentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal UserPrincipal user) {

        DocumentResponse response = documentService.upload(
                user.getTenantId().toString(),
                shipmentId.toString(),
                user.getId() != null ? user.getId().toString() : null,
                type,
                description,
                file
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── List ────────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/shipments/{id}/documents")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(
            summary = "List documents attached to a shipment",
            description = """
                Returns all active documents for the given shipment.
                Each document includes a 1-hour pre-signed download URL.
                The URL must not be cached beyond its TTL.
                """
    )
    public ResponseEntity<List<DocumentResponse>> listByShipment(
            @PathVariable("id") UUID shipmentId,
            @AuthenticationPrincipal UserPrincipal user) {

        List<DocumentResponse> docs = documentService.listByShipment(
                user.getTenantId().toString(),
                shipmentId.toString()
        );
        return ResponseEntity.ok(docs);
    }

    // ── Delete ──────────────────────────────────────────────────────────────

    @DeleteMapping("/api/v1/documents/{id}")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(
            summary = "Soft-delete a document",
            description = """
                Marks the document as inactive (soft delete) and removes the
                underlying object from storage. The metadata record is retained
                for audit purposes. Returns 204 on success.
                """
    )
    public ResponseEntity<Void> delete(
            @PathVariable("id") UUID documentId,
            @AuthenticationPrincipal UserPrincipal user) {

        documentService.delete(user.getTenantId().toString(), documentId.toString());
        return ResponseEntity.noContent().build();
    }
}
