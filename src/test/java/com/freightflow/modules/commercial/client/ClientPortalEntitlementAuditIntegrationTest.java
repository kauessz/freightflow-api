package com.freightflow.modules.commercial.client;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.platform.entitlement.EntitlementDecision;
import com.freightflow.modules.platform.entitlement.EntitlementEnforcementMode;
import com.freightflow.modules.platform.entitlement.EntitlementEnforcementService;
import com.freightflow.modules.platform.entitlement.TenantEntitlementAccessStatus;
import com.freightflow.modules.port.Port;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Client portal entitlement audit rollout")
@TestPropertySource(properties = "freightflow.entitlements.enforcement-mode=AUDIT")
class ClientPortalEntitlementAuditIntegrationTest extends AbstractClientPortalEntitlementIntegrationTest {

    @Autowired
    private EntitlementEnforcementService entitlementEnforcementService;

    private Tenant tenant;
    private User clientUser;

    @BeforeEach
    void setUpData() {
        ensureFeature("COMMERCIAL_RFQ", "Commercial RFQ", "AVAILABLE");
        ensureFeature("CLIENT_PORTAL", "Client Portal", "PARTIAL");
        ensureDependency("CLIENT_PORTAL", "COMMERCIAL_RFQ");

        Port origin = createPort("BRE2U", "Audit Origin", "BR");
        Port destination = createPort("NLE2U", "Audit Destination", "NL");

        tenant = createTenant("Audit");
        Customer customer = createCustomer(tenant, "Audit Customer");
        clientUser = createClientUser(tenant, customer, "Audit");
        createRfq(tenant, customer, clientUser, origin, destination, "RFQ-AUDIT-1", RfqStatus.DRAFT);
    }

    @Test
    @DisplayName("tenantSemSubscriptionContinuaAcessandoQuandoRolloutAudit")
    void tenantSemSubscriptionContinuaAcessandoQuandoRolloutAudit() throws Exception {
        EntitlementDecision decision = entitlementEnforcementService.check(tenant.getId(), "CLIENT_PORTAL");

        assertThat(decision.enforcementMode()).isEqualTo(EntitlementEnforcementMode.AUDIT);
        assertThat(decision.allowed()).isTrue();
        assertThat(decision.allowedByRollout()).isTrue();
        assertThat(decision.entitled()).isFalse();
        assertThat(decision.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.NO_SUBSCRIPTION);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs")
                        .with(asUser(clientUser)))
                .andExpect(status().isOk());
    }
}
