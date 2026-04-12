package com.freightflow.modules.alert;

import com.freightflow.modules.alert.dto.AlertResponse;
import com.freightflow.modules.alert.dto.CreateAlertRequest;
import com.freightflow.shared.rbac.RequiresRole;
import com.freightflow.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Alerts", description = "Shipment alert management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * GET /api/v1/alerts?resolved=false
     * Lista alerts do tenant. Por padrão retorna apenas os em aberto.
     */
    @GetMapping("/api/v1/alerts")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(summary = "List open alerts", description = "Returns all unresolved alerts for the caller's tenant.")
    public ResponseEntity<List<AlertResponse>> listOpen(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(alertService.findOpenByTenant(user.getTenantId()));
    }

    /**
     * GET /api/v1/shipments/{shipmentId}/alerts
     * Lista todos os alerts de um embarque específico (histórico completo).
     */
    @GetMapping("/api/v1/shipments/{shipmentId}/alerts")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "List alerts by shipment", description = "Returns all alerts (resolved and open) for a specific shipment.")
    public ResponseEntity<List<AlertResponse>> listByShipment(
            @PathVariable UUID shipmentId) {
        return ResponseEntity.ok(alertService.findByShipment(shipmentId));
    }

    /**
     * POST /api/v1/alerts
     * Cria um novo alert para um embarque.
     */
    @PostMapping("/api/v1/alerts")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Create alert", description = "Creates a new alert for a shipment. Prevents duplicate open alerts of the same type.")
    public ResponseEntity<AlertResponse> create(
            @Valid @RequestBody CreateAlertRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        AlertResponse response = alertService.create(request, user.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/alerts/{id}/resolve
     * Marca um alert como resolvido.
     */
    @PostMapping("/api/v1/alerts/{id}/resolve")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Resolve alert", description = "Marks an alert as resolved. Validates tenant ownership.")
    public ResponseEntity<AlertResponse> resolve(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(alertService.resolve(id, user.getTenantId()));
    }
}
