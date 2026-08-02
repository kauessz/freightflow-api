package com.freightflow.modules.platform.entitlement;

import com.freightflow.modules.platform.entitlement.dto.TenantEntitlementResolutionResponse;
import com.freightflow.shared.exception.UnauthorizedException;
import com.freightflow.shared.security.platform.PlatformPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform/tenants/{tenantId}/entitlements")
@Tag(name = "Platform Tenant Entitlements", description = "Platform-only diagnostic endpoint for effective tenant entitlements.")
@SecurityRequirement(name = "Platform Bearer Authentication")
public class PlatformTenantEntitlementController {

    private final TenantEntitlementResolverService tenantEntitlementResolverService;

    public PlatformTenantEntitlementController(TenantEntitlementResolverService tenantEntitlementResolverService) {
        this.tenantEntitlementResolverService = tenantEntitlementResolverService;
    }

    @GetMapping
    @Operation(
            summary = "Resolve effective tenant entitlements",
            description = "Returns the effective tenant entitlements derived only from the open tenant subscription, its plan, plan entitlements and platform feature dependencies. accessStatus may be ACTIVE, SUSPENDED, NO_SUBSCRIPTION or INCONSISTENT_SUBSCRIPTION. unlimited is operational and only becomes true when an INTEGER_LIMIT entitlement is effectively enabled with no numeric limit.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tenant entitlement resolution"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid platform token", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "Tenant not found", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<TenantEntitlementResolutionResponse> getTenantEntitlements(Authentication authentication,
                                                                                      @PathVariable UUID tenantId) {
        requirePlatformPrincipal(authentication);
        TenantEntitlementResolution resolution = tenantEntitlementResolverService.resolveTenantEntitlements(tenantId);
        return ResponseEntity.ok(TenantEntitlementResponseMapper.toResponse(resolution));
    }

    private PlatformPrincipal requirePlatformPrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof PlatformPrincipal principal)) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal;
    }
}
