package com.freightflow.modules.commercial.client.quotation;

import com.freightflow.modules.commercial.client.quotation.dto.ClientQuotationResponse;
import com.freightflow.modules.commercial.client.quotation.dto.ClientQuotationSummaryResponse;
import com.freightflow.shared.pagination.PageResponse;
import com.freightflow.shared.rbac.RequiresRole;
import com.freightflow.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/client/quotations")
@Tag(name = "Client Quotations", description = "Customer portal quotation endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientQuotationController {

    private final ClientQuotationService clientQuotationService;

    public ClientQuotationController(ClientQuotationService clientQuotationService) {
        this.clientQuotationService = clientQuotationService;
    }

    @GetMapping
    @RequiresRole("CLIENT")
    @Operation(
            summary = "List quotations available to the authenticated customer",
            description = "Exclusive to CLIENT users. Returns only SENT quotations from the authenticated tenant and customerId. Client DTOs deliberately omit costs, suppliers, internal notes, profit, margin and markup."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SENT quotations listed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only CLIENT users can access this endpoint")
    })
    public ResponseEntity<PageResponse<ClientQuotationSummaryResponse>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sentAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(clientQuotationService.list(user.getTenantId(), user.getCustomerId(), pageable));
    }

    @GetMapping("/{id}")
    @RequiresRole("CLIENT")
    @Operation(
            summary = "Get quotation available to the authenticated customer",
            description = "Exclusive to CLIENT users. Only quotations in SENT status are visible. Resources outside the authenticated tenant/customer scope return 404."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SENT quotation returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Only CLIENT users can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "Quotation not found in the authenticated customer scope or not in SENT status")
    })
    public ResponseEntity<ClientQuotationResponse> getById(@PathVariable UUID id,
                                                           @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(clientQuotationService.getById(id, user.getTenantId(), user.getCustomerId()));
    }
}
