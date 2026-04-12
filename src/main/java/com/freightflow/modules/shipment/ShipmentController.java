package com.freightflow.modules.shipment;

import com.freightflow.modules.shipment.dto.CreateShipmentRequest;
import com.freightflow.modules.shipment.dto.ShipmentResponse;
import com.freightflow.modules.shipment.dto.ShipmentStatsResponse;
import com.freightflow.modules.shipment.dto.UpdateShipmentRequest;
import com.freightflow.modules.shipment.service.ShipmentService;
import com.freightflow.shared.pagination.PageResponse;
import com.freightflow.shared.rbac.RequiresRole;
import com.freightflow.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
@Tag(name = "Shipments", description = "Shipment management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "List shipments", description = "Paginated list. CLIENT role sees only their customer's shipments.")
    public ResponseEntity<PageResponse<ShipmentResponse>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        if ("CLIENT".equals(user.getRole()) && user.getCustomerId() != null) {
            return ResponseEntity.ok(shipmentService.listForClient(user.getTenantId(), user.getCustomerId(), pageable));
        }
        return ResponseEntity.ok(shipmentService.list(user.getTenantId(), pageable));
    }

    @GetMapping("/stats")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get shipment KPI stats", description = "Returns total, inTransit, arrived, delayed and atRisk counts for the tenant")
    public ResponseEntity<ShipmentStatsResponse> getStats(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(shipmentService.getStats(user.getTenantId()));
    }

    @GetMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "Get shipment by ID",
               description = "Returns shipment only if it belongs to the caller's tenant (prevents IDOR).")
    public ResponseEntity<ShipmentResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(shipmentService.getById(id, user.getTenantId()));
    }

    @PostMapping
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Create a new shipment")
    public ResponseEntity<ShipmentResponse> create(
            @Valid @RequestBody CreateShipmentRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        ShipmentResponse response = shipmentService.create(request, user.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Update an existing shipment")
    public ResponseEntity<ShipmentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresRole("ADMIN")
    @Operation(summary = "Delete a shipment")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
