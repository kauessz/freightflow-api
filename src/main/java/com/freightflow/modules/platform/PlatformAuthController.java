package com.freightflow.modules.platform;

import com.freightflow.modules.platform.dto.PlatformAuthResponse;
import com.freightflow.modules.platform.dto.PlatformLoginRequest;
import com.freightflow.modules.platform.dto.PlatformMeResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform Authentication", description = "Platform-only authentication endpoints. Platform tokens never grant tenant access.")
public class PlatformAuthController {

    private final PlatformAuthenticationService platformAuthenticationService;

    public PlatformAuthController(PlatformAuthenticationService platformAuthenticationService) {
        this.platformAuthenticationService = platformAuthenticationService;
    }

    @PostMapping("/auth/login")
    @Operation(
            summary = "Authenticate platform user",
            description = "Validates platform administrator credentials and returns a platform-only JWT.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Platform login succeeded"),
                    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials or disabled platform user", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<PlatformAuthResponse> login(@Valid @RequestBody PlatformLoginRequest request) {
        return ResponseEntity.ok(platformAuthenticationService.login(request));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Platform Bearer Authentication")
    @Operation(
            summary = "Get current platform user",
            description = "Returns the authenticated platform identity. Platform tokens never include tenant or customer scope.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authenticated platform user"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid platform token", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Token authenticated in a different namespace", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<PlatformMeResponse> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof PlatformPrincipal principal)) {
            throw new com.freightflow.shared.exception.UnauthorizedException("Authentication required");
        }
        return ResponseEntity.ok(platformAuthenticationService.me(principal.getId()));
    }
}
