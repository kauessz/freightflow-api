package com.freightflow.modules.webhook;

import com.freightflow.modules.webhook.dto.CreateWebhookRequest;
import com.freightflow.modules.webhook.dto.UpdateWebhookRequest;
import com.freightflow.modules.webhook.dto.WebhookResponse;
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

/**
 * REST endpoints for managing webhook subscriptions.
 *
 * All endpoints require ADMIN role — webhook management is a privileged operation.
 * The tenantId is extracted from the JWT via {@link UserPrincipal}.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Outbound webhook subscription management")
@SecurityRequirement(name = "Bearer Authentication")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * GET /api/v1/webhooks
     * Lista todas as subscriptions do tenant (ativas e inativas).
     */
    @GetMapping
    @RequiresRole("ADMIN")
    @Operation(summary = "List webhooks",
               description = "Returns all webhook subscriptions for the caller's tenant.")
    public ResponseEntity<List<WebhookResponse>> list(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(webhookService.list(user.getTenantId()));
    }

    /**
     * POST /api/v1/webhooks
     * Cria uma nova subscription de webhook.
     */
    @PostMapping
    @RequiresRole("ADMIN")
    @Operation(summary = "Create webhook",
               description = "Registers a new webhook endpoint for the caller's tenant.")
    public ResponseEntity<WebhookResponse> create(
            @Valid @RequestBody CreateWebhookRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        WebhookResponse response = webhookService.create(user.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/webhooks/{id}
     * Atualiza uma subscription existente. Apenas campos não-nulos são aplicados.
     */
    @PutMapping("/{id}")
    @RequiresRole("ADMIN")
    @Operation(summary = "Update webhook",
               description = "Partial update — only non-null fields are applied. " +
                             "Returns 404 if the webhook does not belong to the caller's tenant.")
    public ResponseEntity<WebhookResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWebhookRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(webhookService.update(user.getTenantId(), id, request));
    }

    /**
     * DELETE /api/v1/webhooks/{id}
     * Remove uma subscription de webhook do tenant.
     */
    @DeleteMapping("/{id}")
    @RequiresRole("ADMIN")
    @Operation(summary = "Delete webhook",
               description = "Permanently removes a webhook subscription. " +
                             "Returns 404 if it does not belong to the caller's tenant.")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {
        webhookService.delete(user.getTenantId(), id);
        return ResponseEntity.noContent().build();
    }
}
