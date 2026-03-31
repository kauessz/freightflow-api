package com.freightflow.modules.ais;

import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.VoyageTrackingResponse;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.VoyageRepository;
import com.freightflow.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/voyages")
@Tag(name = "AIS Tracking", description = "Voyage tracking with AIS vessel position")
@SecurityRequirement(name = "Bearer Authentication")
public class VoyageTrackingController {

    private final VoyageRepository voyageRepository;
    private final AisClient aisClient;

    public VoyageTrackingController(VoyageRepository voyageRepository, AisClient aisClient) {
        this.voyageRepository = voyageRepository;
        this.aisClient = aisClient;
    }

    @GetMapping("/{id}/tracking")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Get voyage tracking with AIS position",
        description = "Returns voyage details (vessel, ports, ETD, ETA) plus the current "
            + "AIS position of the vessel. If AIS data is unavailable, the position will be "
            + "estimated as the midpoint between origin and destination."
    )
    public ResponseEntity<VoyageTrackingResponse> getVoyageTracking(@PathVariable UUID id) {
        Voyage voyage = voyageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Voyage", id));

        String imo = voyage.getVessel().getImo();
        AisPositionResponse position = aisClient.getPosition(imo);

        // If AIS data unavailable, estimate position at midpoint of route
        if (position == null) {
            double midLat = midpoint(
                voyage.getOriginPort().getLatitude(),
                voyage.getDestinationPort().getLatitude()
            );
            double midLon = midpoint(
                voyage.getOriginPort().getLongitude(),
                voyage.getDestinationPort().getLongitude()
            );
            position = AisPositionResponse.estimated(midLat, midLon);
        }

        return ResponseEntity.ok(VoyageTrackingResponse.from(voyage, position));
    }

    private double midpoint(Double a, Double b) {
        if (a == null || b == null) return 0.0;
        return (a + b) / 2.0;
    }
}
