package com.freightflow.modules.ais;

import com.freightflow.modules.ais.dto.AisPositionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vessels")
@Tag(name = "AIS Tracking", description = "Real-time vessel position via AIS")
@SecurityRequirement(name = "Bearer Authentication")
public class VesselPositionController {

    private final VesselPositionResolver vesselPositionResolver;

    public VesselPositionController(VesselPositionResolver vesselPositionResolver) {
        this.vesselPositionResolver = vesselPositionResolver;
    }

    @GetMapping("/{imo}/position")
    @Operation(
        summary = "Get vessel AIS position",
        description = "Returns the current AIS position for a vessel by IMO number. "
            + "Results are cached briefly and return an explicit position source."
    )
    public ResponseEntity<AisPositionResponse> getPosition(@PathVariable String imo) {
        return ResponseEntity.ok(vesselPositionResolver.resolveByImo(imo));
    }
}
