package com.freightflow.modules.document.dto;

/**
 * Auxiliary record carrying the non-file fields from a multipart upload request.
 *
 * <p>The {@code file} itself is received as {@code MultipartFile} directly in the
 * controller. This record groups the remaining form fields for readability.</p>
 *
 * @param type        Document type name — one of CTE, BL, NF, OTHER
 * @param description Optional free-text description (max 500 chars)
 */
public record DocumentUploadRequest(
        String type,
        String description
) {}
