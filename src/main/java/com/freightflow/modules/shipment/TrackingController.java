package com.freightflow.modules.shipment;

import com.freightflow.modules.shipment.dto.PublicTrackingResponse;
import com.freightflow.modules.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tracking")
@Tag(name = "Tracking", description = "Public shipment tracking (no auth required)")
public class TrackingController {

    private final ShipmentService shipmentService;

    public TrackingController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/{booking}")
    @Operation(summary = "Track shipment by booking number", description = "Public endpoint — no authentication required")
    public ResponseEntity<PublicTrackingResponse> track(@PathVariable String booking) {
        return ResponseEntity.ok(shipmentService.track(booking));
    }
}
