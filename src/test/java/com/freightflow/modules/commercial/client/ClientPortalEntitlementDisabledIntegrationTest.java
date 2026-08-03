package com.freightflow.modules.commercial.client;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.platform.entitlement.EntitlementDecision;
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

@DisplayName("Client portal entitlement disabled rollout")
@TestPropertySource(properties = "freightflow.entitlements.enforcement-mode=DISABLED")
class ClientPortalEntitlementDisabledIntegrationTest extends AbstractClientPortalEntitlementIntegrationTest {

    @Autowired
    private EntitlementEnforcementService entitlementEnforcementService;

    private Tenant tenant;
    private Customer customer;
    private User clientUser;
    private Quotation sentQuotation;

    @BeforeEach
    void setUpData() {
        ensureFeature("COMMERCIAL_RFQ", "Commercial RFQ", "AVAILABLE");
        ensureFeature("CLIENT_PORTAL", "Client Portal", "PARTIAL");
        ensureDependency("CLIENT_PORTAL", "COMMERCIAL_RFQ");

        Port origin = createPort("BRE2D", "Disabled Origin", "BR");
        Port destination = createPort("NLE2D", "Disabled Destination", "NL");

        tenant = createTenant("Disabled");
        customer = createCustomer(tenant, "Disabled Customer");
        clientUser = createClientUser(tenant, customer, "Disabled");
        User admin = createAdminUser(tenant, "Disabled");
        RequestForQuotation rfq = createRfq(tenant, customer, clientUser, origin, destination, "RFQ-DISABLED-1", RfqStatus.QUOTED);
        sentQuotation = createQuotation(tenant, rfq, admin, "Q-DISABLED-SENT-1", QuotationStatus.SENT);
    }

    @Test
    @DisplayName("tenantSemSubscriptionContinuaAcessandoQuandoRolloutDisabled")
    void tenantSemSubscriptionContinuaAcessandoQuandoRolloutDisabled() throws Exception {
        EntitlementDecision decision = entitlementEnforcementService.check(tenant.getId(), "CLIENT_PORTAL");

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.allowedByRollout()).isTrue();
        assertThat(decision.entitled()).isFalse();
        assertThat(decision.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.NO_SUBSCRIPTION);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations/{id}", sentQuotation.getId())
                        .with(asUser(clientUser)))
                .andExpect(status().isOk());
    }
}
