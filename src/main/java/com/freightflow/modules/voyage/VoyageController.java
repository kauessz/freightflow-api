package com.freightflow.modules.voyage;

import com.freightflow.modules.shipment.dto.ShipmentSummaryResponse;
import com.freightflow.modules.voyage.dto.CreateVoyageRequest;
import com.freightflow.modules.voyage.dto.UpdateVoyageRequest;
import com.freightflow.modules.voyage.dto.VoyageResponse;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/voyages")
@Tag(name = "Voyages", description = "Voyage management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class VoyageController {

    private final VoyageService voyageService;

    public VoyageController(VoyageService voyageService) {
        this.voyageService = voyageService;
    }

    @GetMapping
    @Operation(summary = "List voyages", description = "Paginated list of voyages ordered by ETD descending")
    public ResponseEntity<PageResponse<VoyageResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "etd"));
        return ResponseEntity.ok(voyageService.list(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get voyage by ID")
    public ResponseEntity<VoyageResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(voyageService.getById(id));
    }

    @GetMapping("/number/{voyageNumber}")
    @Operation(summary = "Get voyage by voyage number", description = "Lookup by voyage number (e.g., MSC-2026-001)")
    public ResponseEntity<VoyageResponse> getByVoyageNumber(@PathVariable String voyageNumber) {
        return ResponseEntity.ok(voyageService.getByVoyageNumber(voyageNumber));
    }

    @PostMapping
    @Operation(summary = "Create a new voyage")
    public ResponseEntity<VoyageResponse> create(@Valid @RequestBody CreateVoyageRequest request) {
        VoyageResponse response = voyageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing voyage", description = "Partial update — only non-null fields are applied")
    public ResponseEntity<VoyageResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVoyageRequest request) {
        return ResponseEntity.ok(voyageService.update(id, request));
    }

    @GetMapping("/{id}/shipments")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "List shipments for a voyage",
               description = "Returns the tenant's shipments on a given voyage. CLIENT role is further filtered by customerId.")
    public ResponseEntity<List<ShipmentSummaryResponse>> getShipments(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {
        UUID customerId = "CLIENT".equals(user.getRole()) ? user.getCustomerId() : null;
        List<ShipmentSummaryResponse> shipments = voyageService.getShipmentsByVoyage(id, user.getTenantId(), customerId);
        return ResponseEntity.ok(shipments);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a voyage", description = "Only allowed if voyage has no shipments")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        voyageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
