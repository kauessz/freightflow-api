package com.freightflow.modules.voyage;

import com.freightflow.modules.ais.VesselPositionResolver;
import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.PositionSource;
import com.freightflow.modules.shipment.dto.ShipmentSummaryResponse;
import com.freightflow.modules.voyage.dto.CreateVoyageRequest;
import com.freightflow.modules.voyage.dto.RevisedEtaResponse;
import com.freightflow.modules.voyage.dto.VoyageFleetMapReadinessResponse;
import com.freightflow.modules.voyage.dto.UpdateVoyageRequest;
import com.freightflow.modules.voyage.dto.VoyageResponse;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/voyages")
@Tag(name = "Voyages", description = "Voyage management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class VoyageController {

    private final VoyageService          voyageService;
    private final VoyageRepository       voyageRepository;
    private final VesselPositionResolver vesselPositionResolver;
    private final EtaCalculatorService   etaCalculatorService;

    public VoyageController(VoyageService voyageService,
                            VoyageRepository voyageRepository,
                            VesselPositionResolver vesselPositionResolver,
                            EtaCalculatorService etaCalculatorService) {
        this.voyageService          = voyageService;
        // VoyageRepository injected directly to access the Voyage entity for
        // VesselPositionResolver.resolveForVoyage() without exposing entity-returning
        // methods on VoyageService.
        this.voyageRepository       = voyageRepository;
        this.vesselPositionResolver = vesselPositionResolver;
        this.etaCalculatorService   = etaCalculatorService;
    }

    @GetMapping
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "List voyages", description = "Paginated list of voyages ordered by ETD descending")
    public ResponseEntity<PageResponse<VoyageResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "etd"));
        return ResponseEntity.ok(voyageService.list(pageable));
    }

    @GetMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get voyage by ID")
    public ResponseEntity<VoyageResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(voyageService.getById(id));
    }

    @GetMapping("/number/{voyageNumber}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get voyage by voyage number", description = "Lookup by voyage number (e.g., MSC-2026-001)")
    public ResponseEntity<VoyageResponse> getByVoyageNumber(@PathVariable String voyageNumber) {
        return ResponseEntity.ok(voyageService.getByVoyageNumber(voyageNumber));
    }

    @PostMapping
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Create a new voyage")
    public ResponseEntity<VoyageResponse> create(@Valid @RequestBody CreateVoyageRequest request) {
        VoyageResponse response = voyageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR"})
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

    /**
     * GET /api/v1/voyages/{id}/eta
     *
     * Returns a dynamically revised ETA calculated from the vessel's current AIS position
     * using the Haversine formula.
     *
     * HTTP 422 Unprocessable Entity is returned when:
     *   - The voyage's vessel has no IMO registered, or
     *   - The AIS service cannot provide a position for this vessel.
     */
    @GetMapping("/{id}/eta")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(
            summary = "Dynamic ETA",
            description = "Recalculates the voyage ETA from the vessel's live or estimated AIS position. " +
                          "Returns 422 if no AIS position is available."
    )
    public ResponseEntity<Object> getEta(@PathVariable UUID id) {
        // Load entity with vessel + ports (required for position resolver and ETA calculator)
        Voyage voyage = voyageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", id));

        String imo = voyage.getVessel() != null ? voyage.getVessel().getImo() : null;
        if (imo == null || imo.isBlank()) {
            throw new BusinessException("Vessel has no IMO — AIS unavailable");
        }

        AisPositionResponse position = vesselPositionResolver.resolveForVoyage(voyage);

        if (position.positionSource() == PositionSource.UNAVAILABLE) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of(
                            "statusCode", 422,
                            "error",      "Unprocessable Entity",
                            "message",    "AIS position unavailable for this vessel"
                    ));
        }

        RevisedEtaResponse response = etaCalculatorService.calculate(voyage, position);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @RequiresRole("ADMIN")
    @Operation(summary = "Delete a voyage", description = "Only allowed if voyage has no shipments")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        voyageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fleet-map-readiness")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "List voyage Fleet Map readiness", description = "Returns voyages with readiness status and ineligibility reasons for the caller tenant.")
    public ResponseEntity<List<VoyageFleetMapReadinessResponse>> listFleetMapReadiness(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Boolean eligible) {
        UUID customerId = "CLIENT".equals(user.getRole()) ? user.getCustomerId() : null;
        return ResponseEntity.ok(voyageService.listFleetMapReadiness(user.getTenantId(), customerId, eligible));
    }
}
