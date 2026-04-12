package com.freightflow.modules.ais;

import com.freightflow.modules.ais.dto.VoyageTrackingResponse;
import com.freightflow.modules.voyage.VoyageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/voyages")
@Tag(name = "AIS Tracking", description = "Voyage tracking with AIS vessel position")
@SecurityRequirement(name = "Bearer Authentication")
public class VoyageTrackingController {

    private final VoyageService voyageService;

    public VoyageTrackingController(VoyageService voyageService) {
        this.voyageService = voyageService;
    }

    @GetMapping("/{id}/tracking")
    @Operation(
        summary = "Get voyage tracking with AIS position",
        description = "Returns voyage details (vessel, ports, ETD, ETA) plus the current "
            + "AIS position of the vessel. If AIS data is unavailable, the position will be "
            + "estimated as the midpoint between origin and destination."
    )
    public ResponseEntity<VoyageTrackingResponse> getVoyageTracking(@PathVariable UUID id) {
        return ResponseEntity.ok(voyageService.getTracking(id));
    }
}
