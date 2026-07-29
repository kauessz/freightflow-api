package com.freightflow.modules.commercial.quotation;

import com.freightflow.modules.commercial.quotation.dto.CreateQuotationItemRequest;
import com.freightflow.modules.commercial.quotation.dto.CreateQuotationRequest;
import com.freightflow.modules.commercial.quotation.dto.QuotationFilterParams;
import com.freightflow.modules.commercial.quotation.dto.QuotationResponse;
import com.freightflow.modules.commercial.quotation.dto.QuotationSummaryResponse;
import com.freightflow.modules.commercial.quotation.dto.UpdateQuotationItemRequest;
import com.freightflow.modules.commercial.quotation.dto.UpdateQuotationRequest;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.shared.pagination.PageResponse;
import com.freightflow.shared.rbac.RequiresRole;
import com.freightflow.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@Tag(name = "Commercial Quotations", description = "Internal logistics quotation management")
@SecurityRequirement(name = "Bearer Authentication")
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    @PostMapping("/api/v1/commercial/rfqs/{rfqId}/quotations")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Create quotation for RFQ already in internal analysis")
    public ResponseEntity<QuotationResponse> create(@PathVariable UUID rfqId,
                                                    @Valid @RequestBody CreateQuotationRequest request,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quotationService.create(rfqId, request, user.getTenantId(), user.getId()));
    }

    @GetMapping("/api/v1/commercial/quotations")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "List quotations")
    public ResponseEntity<PageResponse<QuotationSummaryResponse>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) QuotationStatus status,
            @RequestParam(required = false) UUID rfqId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID createdBy,
            @RequestParam(required = false) Instant validFrom,
            @RequestParam(required = false) Instant validTo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        QuotationFilterParams filters = new QuotationFilterParams(search, status, rfqId, customerId, createdBy, validFrom, validTo);
        return ResponseEntity.ok(quotationService.list(user.getTenantId(), filters, pageable));
    }

    @GetMapping("/api/v1/commercial/quotations/{id}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get quotation by ID")
    public ResponseEntity<QuotationResponse> getById(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quotationService.getById(id, user.getTenantId()));
    }

    @PatchMapping("/api/v1/commercial/quotations/{id}")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Update quotation draft")
    public ResponseEntity<QuotationResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateQuotationRequest request,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quotationService.update(id, request, user.getTenantId()));
    }

    @PostMapping("/api/v1/commercial/quotations/{id}/items")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Add quotation item")
    public ResponseEntity<QuotationResponse> addItem(@PathVariable UUID id,
                                                     @Valid @RequestBody CreateQuotationItemRequest request,
                                                     @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quotationService.addItem(id, request, user.getTenantId()));
    }

    @PatchMapping("/api/v1/commercial/quotations/{id}/items/{itemId}")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Update quotation item")
    public ResponseEntity<QuotationResponse> updateItem(@PathVariable UUID id,
                                                        @PathVariable UUID itemId,
                                                        @Valid @RequestBody UpdateQuotationItemRequest request,
                                                        @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quotationService.updateItem(id, itemId, request, user.getTenantId()));
    }

    @DeleteMapping("/api/v1/commercial/quotations/{id}/items/{itemId}")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Delete quotation item")
    public ResponseEntity<QuotationResponse> deleteItem(@PathVariable UUID id,
                                                        @PathVariable UUID itemId,
                                                        @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quotationService.deleteItem(id, itemId, user.getTenantId()));
    }

    @PostMapping("/api/v1/commercial/quotations/{id}/ready-for-review")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Move quotation draft to internal review without changing RFQ to QUOTED")
    public ResponseEntity<QuotationResponse> readyForReview(@PathVariable UUID id,
                                                            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quotationService.readyForReview(id, user.getTenantId()));
    }

    @PostMapping("/api/v1/commercial/quotations/{id}/approve")
    @RequiresRole("ADMIN")
    @Operation(
            summary = "Approve quotation for internal release",
            description = "Restricted to ADMIN. Only quotations in READY_FOR_REVIEW can transition to APPROVED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quotation approved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN can approve quotations"),
            @ApiResponse(responseCode = "404", description = "Quotation not found in the authenticated tenant"),
            @ApiResponse(responseCode = "409", description = "Quotation is not in READY_FOR_REVIEW")
    })
    public ResponseEntity<QuotationResponse> approve(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quotationService.approve(id, user.getTenantId(), user.getId()));
    }

    @PostMapping("/api/v1/commercial/quotations/{id}/send")
    @RequiresRole("ADMIN")
    @Operation(
            summary = "Make approved quotation available to the customer portal",
            description = "Restricted to ADMIN. Requires quotation in APPROVED and related RFQ in UNDER_ANALYSIS. On success, quotation becomes SENT and the RFQ becomes QUOTED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quotation sent successfully and RFQ moved to QUOTED"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN can send quotations"),
            @ApiResponse(responseCode = "404", description = "Quotation not found in the authenticated tenant"),
            @ApiResponse(responseCode = "409", description = "Quotation is not in APPROVED or RFQ is not in UNDER_ANALYSIS")
    })
    public ResponseEntity<QuotationResponse> send(@PathVariable UUID id,
                                                  @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quotationService.send(id, user.getTenantId(), user.getId()));
    }

    @PostMapping("/api/v1/commercial/quotations/{id}/cancel")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Cancel quotation")
    public ResponseEntity<QuotationResponse> cancel(@PathVariable UUID id,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quotationService.cancel(id, user.getTenantId()));
    }
}
