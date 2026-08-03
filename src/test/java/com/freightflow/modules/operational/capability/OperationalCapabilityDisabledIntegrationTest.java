package com.freightflow.modules.operational.capability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Operational capability disabled integration")
@TestPropertySource(properties = "freightflow.entitlements.enforcement-mode=DISABLED")
class OperationalCapabilityDisabledIntegrationTest extends OperationalCapabilityIntegrationSupport {

    @BeforeEach
    void setUpFixture() {
        ensureOperationalCapabilityCatalog();
    }

    @Test
    @DisplayName("tenantSemSubscriptionRecebeTresTrue")
    void tenantSemSubscriptionRecebeTresTrue() throws Exception {
        String token = registerAndLogin("Disabled Admin", "disabled-capability@tenant.test", "Tenant1234", "Capability Disabled");

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capabilities[0].available").value(true))
                .andExpect(jsonPath("$.capabilities[1].available").value(true))
                .andExpect(jsonPath("$.capabilities[2].available").value(true));
    }
}
