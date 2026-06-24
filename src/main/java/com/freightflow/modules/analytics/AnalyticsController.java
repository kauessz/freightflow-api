package com.freightflow.modules.analytics;

import com.freightflow.modules.analytics.dto.DelayStatsResponse;
import com.freightflow.modules.analytics.dto.OperationsDashboardResponse;
import com.freightflow.modules.analytics.dto.PerformanceResponse;
import com.freightflow.shared.rbac.RequiresRole;
import com.freightflow.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Analytics REST controller.
 *
 * All endpoints are scoped to the authenticated user's tenant via
 * {@link UserPrincipal#getTenantId()}.
 *
 * Access is restricted to ADMIN, OPERATOR, and VIEWER roles.
 * The CLIENT role is intentionally excluded — clients see only their
 * own shipment data through the Shipments module, not aggregated KPIs.
 *
 * Results are cached for 2 minutes (see "analytics-dashboard" in CacheConfig).
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Operational analytics and KPI endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/v1/analytics/operations-dashboard
     *
     * Returns aggregated KPIs: active shipment counts (total, in-transit, delayed,
     * at-risk, awaiting-docs), alert counts (open, critical, high),
     * and breakdowns by status and carrier (top 5).
     */
    @GetMapping("/operations-dashboard")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(
            summary = "Operations dashboard",
            description = "Aggregated KPIs for the operations overview page. " +
                          "Returns shipment counts, alert counts, status distribution, " +
                          "and top-5 carrier breakdown. Cached for 2 minutes per tenant."
    )
    public ResponseEntity<OperationsDashboardResponse> operationsDashboard(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(analyticsService.getOperationsDashboard(user.getTenantId()));
    }

    /**
     * GET /api/v1/analytics/delays
     *
     * Returns the overall delay rate and top-5 breakdowns by route and vessel.
     */
    @GetMapping("/delays")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(
            summary = "Delay statistics",
            description = "Overall delay rate and top-5 delay rankings by origin→destination route " +
                          "and by vessel. Useful for identifying systemic delay patterns."
    )
    public ResponseEntity<DelayStatsResponse> delayStats(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(analyticsService.getDelayStats(user.getTenantId()));
    }

    /**
     * GET /api/v1/analytics/performance
     *
     * Returns on-time delivery rate, average delay days, total delivered/cancelled,
     * and on-time rate per carrier.
     */
    @GetMapping("/performance")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER"})
    @Operation(
            summary = "Performance metrics",
            description = "On-time delivery rate, average delay, and per-carrier on-time breakdown " +
                          "based on DELIVERED shipments. Returns 100.0 on-time rate if no deliveries exist yet."
    )
    public ResponseEntity<PerformanceResponse> performance(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(analyticsService.getPerformance(user.getTenantId()));
    }
}
