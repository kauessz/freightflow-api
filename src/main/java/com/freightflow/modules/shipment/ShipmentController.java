package com.freightflow.modules.shipment;

import com.freightflow.modules.shipment.dto.CreateShipmentRequest;
import com.freightflow.modules.shipment.dto.ShipmentResponse;
import com.freightflow.modules.shipment.dto.UpdateShipmentRequest;
import com.freightflow.modules.shipment.service.ShipmentService;
import com.freightflow.shared.pagination.PageResponse;
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
    @Operation(summary = "List shipments", description = "Paginated list filtered by tenant")
    public ResponseEntity<PageResponse<ShipmentResponse>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(shipmentService.list(user.getTenantId(), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shipment by ID")
    public ResponseEntity<ShipmentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new shipment")
    public ResponseEntity<ShipmentResponse> create(
            @Valid @RequestBody CreateShipmentRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        ShipmentResponse response = shipmentService.create(request, user.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing shipment")
    public ResponseEntity<ShipmentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a shipment")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/events")
    @Operation(summary = "List shipment events")
    public ResponseEntity<Void> listEvents(@PathVariable UUID id) {
        // TODO: Implementar no modulo event
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/events")
    @Operation(summary = "Register a new event for a shipment")
    public ResponseEntity<Void> addEvent(@PathVariable UUID id) {
        // TODO: Implementar no modulo event
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
