package com.freightflow.modules.commercial.client.rfq;

import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqCreateRequest;
import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqResponse;
import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqSummaryResponse;
import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqUpdateRequest;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/client/rfqs")
@Tag(name = "Client RFQs", description = "Customer portal RFQ endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientRfqController {

    private final ClientRfqService clientRfqService;

    public ClientRfqController(ClientRfqService clientRfqService) {
        this.clientRfqService = clientRfqService;
    }

    @PostMapping
    @RequiresRole("CLIENT")
    @Operation(
            summary = "Create RFQ for the authenticated customer",
            description = "Exclusive to CLIENT users. The RFQ is always scoped to the authenticated tenant and customerId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "RFQ created for the authenticated customer"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only CLIENT users can access this endpoint"),
            @ApiResponse(responseCode = "409", description = "Invalid RFQ transition or business rule violation")
    })
    public ResponseEntity<ClientRfqResponse> create(@Valid @RequestBody ClientRfqCreateRequest request,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientRfqService.create(request, user.getTenantId(), user.getCustomerId(), user.getId()));
    }

    @GetMapping
    @RequiresRole("CLIENT")
    @Operation(
            summary = "List RFQs for the authenticated customer",
            description = "Exclusive to CLIENT users. Returns only RFQs from the authenticated tenant and authenticated customerId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RFQs listed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only CLIENT users can access this endpoint")
    })
    public ResponseEntity<PageResponse<ClientRfqSummaryResponse>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(clientRfqService.list(user.getTenantId(), user.getCustomerId(), pageable));
    }

    @GetMapping("/{id}")
    @RequiresRole("CLIENT")
    @Operation(
            summary = "Get RFQ detail for the authenticated customer",
            description = "Exclusive to CLIENT users. Resources outside the authenticated tenant/customer scope return 404."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RFQ returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only CLIENT users can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "RFQ not found in the authenticated customer scope")
    })
    public ResponseEntity<ClientRfqResponse> getById(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(clientRfqService.getById(id, user.getTenantId(), user.getCustomerId()));
    }

    @PatchMapping("/{id}")
    @RequiresRole("CLIENT")
    @Operation(
            summary = "Update own RFQ while in DRAFT",
            description = "Exclusive to CLIENT users. Only DRAFT RFQs from the authenticated tenant/customer scope can be updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RFQ updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only CLIENT users can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "RFQ not found in the authenticated customer scope"),
            @ApiResponse(responseCode = "409", description = "RFQ is not in DRAFT or violates a business rule")
    })
    public ResponseEntity<ClientRfqResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody ClientRfqUpdateRequest request,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(clientRfqService.update(id, request, user.getTenantId(), user.getCustomerId()));
    }

    @PostMapping("/{id}/submit")
    @RequiresRole("CLIENT")
    @Operation(
            summary = "Submit own RFQ",
            description = "Exclusive to CLIENT users. Only DRAFT RFQs from the authenticated tenant/customer scope can be submitted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RFQ submitted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only CLIENT users can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "RFQ not found in the authenticated customer scope"),
            @ApiResponse(responseCode = "409", description = "RFQ transition is not allowed")
    })
    public ResponseEntity<ClientRfqResponse> submit(@PathVariable UUID id,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(clientRfqService.submit(id, user.getTenantId(), user.getCustomerId()));
    }

    @PostMapping("/{id}/cancel")
    @RequiresRole("CLIENT")
    @Operation(
            summary = "Cancel own RFQ while in DRAFT or SUBMITTED",
            description = "Exclusive to CLIENT users. Only DRAFT or SUBMITTED RFQs from the authenticated tenant/customer scope can be cancelled."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RFQ cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only CLIENT users can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "RFQ not found in the authenticated customer scope"),
            @ApiResponse(responseCode = "409", description = "RFQ transition is not allowed")
    })
    public ResponseEntity<ClientRfqResponse> cancel(@PathVariable UUID id,
                                                    @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(clientRfqService.cancel(id, user.getTenantId(), user.getCustomerId()));
    }
}
