package com.freightflow.modules.platform.catalog;

import com.freightflow.modules.platform.catalog.dto.PlatformFeatureResponse;
import com.freightflow.modules.platform.catalog.dto.SubscriptionPlanDetailResponse;
import com.freightflow.modules.platform.catalog.dto.SubscriptionPlanSummaryResponse;
import com.freightflow.shared.exception.BadRequestException;
import com.freightflow.shared.exception.UnauthorizedException;
import com.freightflow.shared.pagination.PageResponse;
import com.freightflow.shared.security.platform.PlatformPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform Catalog", description = "Read-only platform catalog for features, plans and entitlements.")
@SecurityRequirement(name = "Platform Bearer Authentication")
public class PlatformCatalogController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PlatformCatalogService platformCatalogService;

    public PlatformCatalogController(PlatformCatalogService platformCatalogService) {
        this.platformCatalogService = platformCatalogService;
    }

    @GetMapping("/features")
    @Operation(
            summary = "List platform features",
            description = "Returns the persisted platform feature catalog. `limitValue = null` only applies to integer-limit features inside plan responses and means unlimited.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feature catalog page"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid platform token", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Token authenticated in a different namespace", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<PageResponse<PlatformFeatureResponse>> listFeatures(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) PlatformFeatureValueType valueType) {
        requirePlatformPrincipal(authentication);
        validatePageRequest(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("key"));
        return ResponseEntity.ok(platformCatalogService.listFeatures(active, valueType, pageable));
    }

    @GetMapping("/features/{key}")
    @Operation(
            summary = "Get platform feature by key",
            description = "Returns one catalog feature using a stable uppercase key. Path lookup is case-insensitive.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feature found"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid platform token", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "Feature not found", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<PlatformFeatureResponse> getFeatureByKey(Authentication authentication,
                                                                   @PathVariable String key) {
        requirePlatformPrincipal(authentication);
        validateCatalogIdentifier("key", key);
        return ResponseEntity.ok(platformCatalogService.getFeatureByKey(key));
    }

    @GetMapping("/plans")
    @Operation(
            summary = "List subscription plans",
            description = "Returns paginated read-only plan summaries. Plans are platform catalog records only in this phase and are not associated to tenants yet.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Plan catalog page"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid platform token", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Token authenticated in a different namespace", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<PageResponse<SubscriptionPlanSummaryResponse>> listPlans(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SubscriptionPlanStatus status) {
        requirePlatformPrincipal(authentication);
        validatePageRequest(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder").and(Sort.by("code")));
        return ResponseEntity.ok(platformCatalogService.listPlans(status, pageable));
    }

    @GetMapping("/plans/{id}")
    @Operation(
            summary = "Get subscription plan by ID",
            description = "Returns the plan with its effective read-only entitlement matrix against the current catalog. Missing rows imply the feature is not granted by that plan.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Plan found"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid platform token", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "Plan not found", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<SubscriptionPlanDetailResponse> getPlanById(Authentication authentication,
                                                                      @PathVariable UUID id) {
        requirePlatformPrincipal(authentication);
        return ResponseEntity.ok(platformCatalogService.getPlanById(id));
    }

    @GetMapping("/plans/code/{code}")
    @Operation(
            summary = "Get subscription plan by code",
            description = "Returns the plan with its entitlement matrix using a stable uppercase code. Path lookup is case-insensitive.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "code", in = ParameterIn.PATH, description = "Plan code such as STARTER or ENTERPRISE")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Plan found"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid platform token", content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "Plan not found", content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<SubscriptionPlanDetailResponse> getPlanByCode(Authentication authentication,
                                                                        @PathVariable String code) {
        requirePlatformPrincipal(authentication);
        validateCatalogIdentifier("code", code);
        return ResponseEntity.ok(platformCatalogService.getPlanByCode(code));
    }

    private PlatformPrincipal requirePlatformPrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof PlatformPrincipal principal)) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal;
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Parameter 'page' must be greater than or equal to 0.");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Parameter 'size' must be between 1 and %d.".formatted(MAX_PAGE_SIZE));
        }
    }

    private void validateCatalogIdentifier(String parameterName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Parameter '%s' must not be blank.".formatted(parameterName));
        }
    }
}
