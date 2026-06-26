package com.freightflow.modules.vessel;

import com.freightflow.modules.vessel.dto.CreateVesselRequest;
import com.freightflow.modules.vessel.dto.PositionTrackPoint;
import com.freightflow.modules.vessel.dto.UpdateVesselRequest;
import com.freightflow.modules.vessel.dto.VesselResponse;
import com.freightflow.modules.vessel.dto.VesselWithVoyageResponse;
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
@RequestMapping("/api/v1/vessels")
@Tag(name = "Vessels", description = "Vessel fleet management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class VesselController {

    private final VesselService          vesselService;
    private final PositionHistoryService positionHistoryService;

    public VesselController(VesselService vesselService,
                            PositionHistoryService positionHistoryService) {
        this.vesselService          = vesselService;
        this.positionHistoryService = positionHistoryService;
    }

    @GetMapping
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "List vessels", description = "Paginated list of vessels ordered by name")
    public ResponseEntity<PageResponse<VesselResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(vesselService.list(pageable));
    }

    @GetMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get vessel by ID")
    public ResponseEntity<VesselResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(vesselService.getById(id));
    }

    @GetMapping("/imo/{imo}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get vessel by IMO number")
    public ResponseEntity<VesselResponse> getByImo(@PathVariable String imo) {
        return ResponseEntity.ok(vesselService.getByImo(imo));
    }

    @GetMapping("/active-with-shipments")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "List active vessels with tenant shipments",
               description = "Returns IN_TRANSIT and DEPARTED voyages that contain at least one shipment " +
                             "from the authenticated tenant, enriched with AIS position and shipment count. " +
                             "Used by the Fleet Map 'My shipments' toggle.")
    public ResponseEntity<List<VesselWithVoyageResponse>> getActiveWithShipments(
            @AuthenticationPrincipal UserPrincipal user) {
        UUID customerId = "CLIENT".equals(user.getRole()) ? user.getCustomerId() : null;
        return ResponseEntity.ok(vesselService.getActiveWithShipments(user.getTenantId(), customerId));
    }

    /**
     * GET /api/v1/vessels/{imo}/track?limit=50
     * Legacy alias: GET /api/v1/vessels/imo/{imo}/track?limit=50
     *
     * Returns the recorded AIS position history for a vessel, derived from
     * POSITION_UPDATE events stored by the PositionTrackingJob and scoped
     * to shipments visible to the authenticated tenant/customer.
     * Points are ordered by event time descending (most recent first).
     *
     * Useful for rendering a breadcrumb trail on a map.
     * Returns an empty list if no POSITION_UPDATE events exist yet.
     */
    @GetMapping({"/{imo}/track", "/imo/{imo}/track"})
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(
            summary = "Vessel position track history",
            description = "Returns up to {limit} AIS position snapshots for the vessel's active shipments, " +
                          "ordered by event time descending. Maximum 200 points. " +
                          "Points are recorded every 5 minutes by the background tracking job."
    )
    public ResponseEntity<List<PositionTrackPoint>> getTrack(
            @PathVariable String imo,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "50") int limit) {
        UUID customerId = "CLIENT".equals(user.getRole()) ? user.getCustomerId() : null;
        List<PositionTrackPoint> track = positionHistoryService.getPositionHistory(
                imo,
                user.getTenantId(),
                customerId,
                limit
        );
        return ResponseEntity.ok(track);
    }

    @PostMapping
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Register a new vessel")
    public ResponseEntity<VesselResponse> create(@Valid @RequestBody CreateVesselRequest request) {
        VesselResponse response = vesselService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Update vessel details")
    public ResponseEntity<VesselResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVesselRequest request) {
        return ResponseEntity.ok(vesselService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresRole("ADMIN")
    @Operation(summary = "Delete a vessel", description = "Only allowed if vessel has no voyages")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        vesselService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
