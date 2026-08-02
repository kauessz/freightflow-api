package com.freightflow.modules.platform.subscription;

import com.freightflow.modules.platform.subscription.dto.AssignTenantSubscriptionRequest;
import com.freightflow.modules.platform.subscription.dto.CancelTenantSubscriptionRequest;
import com.freightflow.modules.platform.subscription.dto.ChangeTenantPlanRequest;
import com.freightflow.modules.platform.subscription.dto.ReactivateTenantSubscriptionRequest;
import com.freightflow.modules.platform.subscription.dto.SuspendTenantSubscriptionRequest;
import com.freightflow.modules.platform.subscription.dto.TenantSubscriptionCurrentResponse;
import com.freightflow.modules.platform.subscription.dto.TenantSubscriptionHistoryResponse;
import com.freightflow.modules.platform.subscription.dto.TenantSubscriptionResponse;
import com.freightflow.shared.exception.UnauthorizedException;
import com.freightflow.shared.security.platform.PlatformPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform/tenants/{tenantId}/subscription")
@Tag(name = "Platform Tenant Subscriptions", description = "Platform-only tenant subscription management endpoints.")
@SecurityRequirement(name = "Platform Bearer Authentication")
public class PlatformTenantSubscriptionController {

    private final PlatformTenantSubscriptionService platformTenantSubscriptionService;

    public PlatformTenantSubscriptionController(PlatformTenantSubscriptionService platformTenantSubscriptionService) {
        this.platformTenantSubscriptionService = platformTenantSubscriptionService;
    }

    @GetMapping
    @Operation(
            summary = "Get current tenant subscription",
            description = "Returns the current open subscription for the tenant. In this phase, open means ACTIVE or SUSPENDED with endedAt = null.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current subscription payload"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid platform token", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "Tenant not found", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<TenantSubscriptionCurrentResponse> getCurrentSubscription(Authentication authentication,
                                                                                    @PathVariable UUID tenantId) {
        requirePlatformPrincipal(authentication);
        return ResponseEntity.ok(platformTenantSubscriptionService.getCurrentSubscription(tenantId));
    }

    @GetMapping("/history")
    @Operation(summary = "Get tenant subscription history")
    public ResponseEntity<TenantSubscriptionHistoryResponse> getHistory(Authentication authentication,
                                                                        @PathVariable UUID tenantId) {
        requirePlatformPrincipal(authentication);
        return ResponseEntity.ok(platformTenantSubscriptionService.getHistory(tenantId));
    }

    @PostMapping("/assign")
    @Operation(summary = "Assign an active plan to a tenant without an open subscription")
    public ResponseEntity<TenantSubscriptionResponse> assign(Authentication authentication,
                                                             @PathVariable UUID tenantId,
                                                             @Valid @RequestBody AssignTenantSubscriptionRequest request) {
        requirePlatformPrincipal(authentication);
        return ResponseEntity.ok(platformTenantSubscriptionService.assignPlan(tenantId, request.planCode(), request.reason()));
    }

    @PostMapping("/change-plan")
    @Operation(summary = "Change the current active plan for a tenant")
    public ResponseEntity<TenantSubscriptionResponse> changePlan(Authentication authentication,
                                                                 @PathVariable UUID tenantId,
                                                                 @Valid @RequestBody ChangeTenantPlanRequest request) {
        requirePlatformPrincipal(authentication);
        return ResponseEntity.ok(platformTenantSubscriptionService.changePlan(tenantId, request.planCode(), request.reason()));
    }

    @PostMapping("/suspend")
    @Operation(summary = "Suspend the current active subscription")
    public ResponseEntity<TenantSubscriptionResponse> suspend(Authentication authentication,
                                                              @PathVariable UUID tenantId,
                                                              @Valid @RequestBody SuspendTenantSubscriptionRequest request) {
        requirePlatformPrincipal(authentication);
        return ResponseEntity.ok(platformTenantSubscriptionService.suspend(tenantId, request.reason()));
    }

    @PostMapping("/reactivate")
    @Operation(summary = "Reactivate the current suspended subscription")
    public ResponseEntity<TenantSubscriptionResponse> reactivate(Authentication authentication,
                                                                 @PathVariable UUID tenantId,
                                                                 @Valid @RequestBody ReactivateTenantSubscriptionRequest request) {
        requirePlatformPrincipal(authentication);
        return ResponseEntity.ok(platformTenantSubscriptionService.reactivate(tenantId, request.reason()));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel the current open subscription")
    public ResponseEntity<TenantSubscriptionResponse> cancel(Authentication authentication,
                                                             @PathVariable UUID tenantId,
                                                             @Valid @RequestBody CancelTenantSubscriptionRequest request) {
        requirePlatformPrincipal(authentication);
        return ResponseEntity.ok(platformTenantSubscriptionService.cancel(tenantId, request.reason()));
    }

    private PlatformPrincipal requirePlatformPrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof PlatformPrincipal principal)) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal;
    }
}
