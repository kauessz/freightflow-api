package com.freightflow.modules.commercial.rfq;

import com.freightflow.modules.commercial.rfq.dto.CreateRfqRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqFilterParams;
import com.freightflow.modules.commercial.rfq.dto.RfqResponse;
import com.freightflow.modules.commercial.rfq.dto.RfqSummaryResponse;
import com.freightflow.modules.commercial.rfq.dto.UpdateRfqRequest;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.shared.pagination.PageResponse;
import com.freightflow.shared.rbac.RequiresRole;
import com.freightflow.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/commercial/rfqs")
@Tag(name = "Commercial RFQs", description = "Internal logistics RFQ management")
@SecurityRequirement(name = "Bearer Authentication")
public class RfqController {

    private final RfqService rfqService;

    public RfqController(RfqService rfqService) {
        this.rfqService = rfqService;
    }

    @PostMapping
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Create RFQ")
    public ResponseEntity<RfqResponse> create(@Valid @RequestBody CreateRfqRequest request,
                                              @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rfqService.create(request, user.getTenantId(), user.getId()));
    }

    @GetMapping
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "List RFQs")
    public ResponseEntity<PageResponse<RfqSummaryResponse>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RfqStatus status,
            @RequestParam(required = false) RfqDirection direction,
            @RequestParam(required = false) RfqServiceType serviceType,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) UUID originPortId,
            @RequestParam(required = false) UUID destinationPortId,
            @RequestParam(required = false) Instant createdFrom,
            @RequestParam(required = false) Instant createdTo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        RfqFilterParams filters = new RfqFilterParams(
                search, status, direction, serviceType, customerId, assignedTo,
                originPortId, destinationPortId, createdFrom, createdTo
        );
        return ResponseEntity.ok(rfqService.list(user.getTenantId(), filters, pageable));
    }

    @GetMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get RFQ by ID")
    public ResponseEntity<RfqResponse> getById(@PathVariable UUID id,
                                               @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(rfqService.getById(id, user.getTenantId()));
    }

    @PatchMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Update RFQ draft")
    public ResponseEntity<RfqResponse> update(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateRfqRequest request,
                                              @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(rfqService.update(id, request, user.getTenantId()));
    }

    @DeleteMapping("/{id}")
    @RequiresRole("ADMIN")
    @Operation(summary = "Delete RFQ draft without quotations")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal UserPrincipal user) {
        rfqService.delete(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Submit RFQ")
    public ResponseEntity<RfqResponse> submit(@PathVariable UUID id,
                                              @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(rfqService.submit(id, user.getTenantId()));
    }

    @PostMapping("/{id}/start-analysis")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Move RFQ to analysis")
    public ResponseEntity<RfqResponse> startAnalysis(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(rfqService.startAnalysis(id, user.getTenantId()));
    }

    @PostMapping("/{id}/cancel")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Cancel RFQ")
    public ResponseEntity<RfqResponse> cancel(@PathVariable UUID id,
                                              @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(rfqService.cancel(id, user.getTenantId()));
    }
}
