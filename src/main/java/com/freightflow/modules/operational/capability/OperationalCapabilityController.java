package com.freightflow.modules.operational.capability;

import com.freightflow.modules.operational.capability.dto.OperationalCapabilitiesResponse;
import com.freightflow.shared.exception.UnauthorizedException;
import com.freightflow.shared.rbac.RequiresRole;
import com.freightflow.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Operational Capabilities", description = "Authenticated tenant capabilities exposed safely for operational frontends.")
@SecurityRequirement(name = "Bearer Authentication")
public class OperationalCapabilityController {

    private final OperationalCapabilityService operationalCapabilityService;

    public OperationalCapabilityController(OperationalCapabilityService operationalCapabilityService) {
        this.operationalCapabilityService = operationalCapabilityService;
    }

    @GetMapping("/capabilities")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(
            summary = "Get operational capabilities for the authenticated tenant",
            description = "Returns only frontend-safe capability flags derived from the authenticated tenant token. Platform diagnostics and subscription internals are intentionally omitted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Capabilities evaluated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required or wrong token namespace"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not belong to an allowed tenant role")
    })
    public ResponseEntity<OperationalCapabilitiesResponse> getCapabilities(
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            throw new UnauthorizedException("Authentication required");
        }

        return ResponseEntity.ok(OperationalCapabilitiesResponse.from(
                operationalCapabilityService.getCapabilities(user.getTenantId())
        ));
    }
}
