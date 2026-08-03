package com.freightflow.modules.operational.capability;

import com.freightflow.modules.platform.entitlement.EntitlementBatchDecision;
import com.freightflow.modules.platform.entitlement.EntitlementDecision;
import com.freightflow.modules.platform.entitlement.EntitlementEnforcementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OperationalCapabilityService {

    public static final List<String> EXPOSED_CAPABILITY_KEYS = List.of(
            "CLIENT_PORTAL",
            "COMMERCIAL_RFQ",
            "QUOTATION_WORKFLOW"
    );

    private final EntitlementEnforcementService entitlementEnforcementService;

    public OperationalCapabilityService(EntitlementEnforcementService entitlementEnforcementService) {
        this.entitlementEnforcementService = entitlementEnforcementService;
    }

    public OperationalCapabilitySnapshot getCapabilities(UUID tenantId) {
        EntitlementBatchDecision decision = entitlementEnforcementService.inspectAll(tenantId, EXPOSED_CAPABILITY_KEYS);

        List<OperationalCapabilityAvailability> capabilities = decision.decisions().stream()
                .map(this::toAvailability)
                .toList();

        return new OperationalCapabilitySnapshot(capabilities, Instant.now());
    }

    private OperationalCapabilityAvailability toAvailability(EntitlementDecision decision) {
        return new OperationalCapabilityAvailability(decision.featureKey(), decision.allowed());
    }
}
