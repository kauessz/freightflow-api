package com.freightflow.modules.platform.entitlement;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "freightflow.entitlements")
public class EntitlementEnforcementProperties {

    private EntitlementEnforcementMode enforcementMode = EntitlementEnforcementMode.DISABLED;

    public EntitlementEnforcementMode getEnforcementMode() {
        return enforcementMode;
    }

    public void setEnforcementMode(EntitlementEnforcementMode enforcementMode) {
        this.enforcementMode = enforcementMode;
    }
}
