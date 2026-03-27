package com.freightflow.modules.event;

import com.freightflow.modules.event.dto.CreateEventRequest;
import com.freightflow.modules.event.dto.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments/{shipmentId}/events")
@Tag(name = "Events", description = "Shipment event tracking — milestones like GATE_IN, LOADED, DEPARTED, ARRIVED")
@SecurityRequirement(name = "Bearer Authentication")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "List events for a shipment", description = "Returns all events ordered by occurredAt descending")
    public ResponseEntity<List<EventResponse>> list(@PathVariable UUID shipmentId) {
        return ResponseEntity.ok(eventService.listByShipment(shipmentId));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get a specific event")
    public ResponseEntity<EventResponse> getById(@PathVariable UUID shipmentId,
                                                  @PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.getById(eventId));
    }

    @PostMapping
    @Operation(summary = "Register a new event",
               description = "Creates a cargo event and automatically updates the shipment status")
    public ResponseEntity<EventResponse> create(
            @PathVariable UUID shipmentId,
            @Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.create(shipmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete an event")
    public ResponseEntity<Void> delete(@PathVariable UUID shipmentId,
                                        @PathVariable UUID eventId) {
        eventService.delete(eventId);
        return ResponseEntity.noContent().build();
    }
}
