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

    private final AisClient aisClient;

    public VesselPositionController(AisClient aisClient) {
        this.aisClient = aisClient;
    }

    @GetMapping("/{imo}/position")
    @Operation(
        summary = "Get vessel AIS position",
        description = "Returns the current AIS position for a vessel by IMO number. "
            + "Results are cached for 5 minutes."
    )
    public ResponseEntity<AisPositionResponse> getPosition(@PathVariable String imo) {
        AisPositionResponse position = aisClient.getPosition(imo);
        if (position == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(position);
    }
}
