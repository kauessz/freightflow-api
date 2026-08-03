package com.freightflow.modules.operational.capability;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Operational capability integration")
@TestPropertySource(properties = "freightflow.entitlements.enforcement-mode=ENFORCE")
class OperationalCapabilityIntegrationTest extends OperationalCapabilityIntegrationSupport {

    @BeforeEach
    void setUpFixture() {
        ensureOperationalCapabilityCatalog();
    }

    @Test
    @DisplayName("tenantCompletoRecebeTresCapabilitiesTrue")
    void tenantCompletoRecebeTresCapabilitiesTrue() throws Exception {
        String password = "Tenant1234";
        String email = "full-admin@tenant.test";
        String token = registerAndLogin("Full Admin", email, password, "Capability Full");
        User user = userRepository.findByEmail(email).orElseThrow();

        var planId = insertPlan("FULL");
        grantFeature(planId, "COMMERCIAL_RFQ");
        grantFeature(planId, "QUOTATION_WORKFLOW");
        grantFeature(planId, "CLIENT_PORTAL");
        insertSubscription(user.getTenant().getId(), planId, "ACTIVE");

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capabilities[0].key").value("CLIENT_PORTAL"))
                .andExpect(jsonPath("$.capabilities[0].available").value(true))
                .andExpect(jsonPath("$.capabilities[1].key").value("COMMERCIAL_RFQ"))
                .andExpect(jsonPath("$.capabilities[1].available").value(true))
                .andExpect(jsonPath("$.capabilities[2].key").value("QUOTATION_WORKFLOW"))
                .andExpect(jsonPath("$.capabilities[2].available").value(true))
                .andExpect(jsonPath("$.tenantId").doesNotExist())
                .andExpect(jsonPath("$.subscriptionId").doesNotExist())
                .andExpect(jsonPath("$.enforcementMode").doesNotExist());
    }

    @Test
    @DisplayName("tenantApenasComCommercialRfqRecebeMatrizEsperadaEEndpointsCoerentes")
    void tenantApenasComCommercialRfqRecebeMatrizEsperadaEEndpointsCoerentes() throws Exception {
        String password = "Tenant1234";
        String email = "rfq-only-admin@tenant.test";
        String token = registerAndLogin("RFQ Admin", email, password, "Capability RFQ Only");
        User admin = userRepository.findByEmail(email).orElseThrow();

        var planId = insertPlan("RFQ_ONLY");
        grantFeature(planId, "COMMERCIAL_RFQ");
        insertSubscription(admin.getTenant().getId(), planId, "ACTIVE");

        User client = createClientUser(admin.getTenant(), "rfq-only-client@tenant.test", password);
        String clientToken = login(client.getEmail(), password);

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capabilities[0].available").value(false))
                .andExpect(jsonPath("$.capabilities[1].available").value(true))
                .andExpect(jsonPath("$.capabilities[2].available").value(false));

        mockMvc.perform(get("/api/v1/commercial/rfqs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/commercial/quotations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("QUOTATION_WORKFLOW"));

        mockMvc.perform(get("/api/v1/client/rfqs")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("CLIENT_PORTAL"));
    }

    @Test
    @DisplayName("tenantSemSubscriptionRecebeTresCapabilitiesFalse")
    void tenantSemSubscriptionRecebeTresCapabilitiesFalse() throws Exception {
        String token = registerAndLogin("No Sub Admin", "no-sub-capability@tenant.test", "Tenant1234", "Capability No Sub");

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capabilities[0].available").value(false))
                .andExpect(jsonPath("$.capabilities[1].available").value(false))
                .andExpect(jsonPath("$.capabilities[2].available").value(false));
    }

    @Test
    @DisplayName("tenantSuspendedRecebeTresCapabilitiesFalse")
    void tenantSuspendedRecebeTresCapabilitiesFalse() throws Exception {
        String email = "suspended-capability@tenant.test";
        String token = registerAndLogin("Suspended Admin", email, "Tenant1234", "Capability Suspended");
        User user = userRepository.findByEmail(email).orElseThrow();

        var planId = insertPlan("SUSPENDED");
        grantFeature(planId, "COMMERCIAL_RFQ");
        grantFeature(planId, "QUOTATION_WORKFLOW");
        grantFeature(planId, "CLIENT_PORTAL");
        insertSubscription(user.getTenant().getId(), planId, "SUSPENDED");

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capabilities[0].available").value(false))
                .andExpect(jsonPath("$.capabilities[1].available").value(false))
                .andExpect(jsonPath("$.capabilities[2].available").value(false));
    }

    @Test
    @DisplayName("dependenciaQuebradaDesabilitaFeaturesDependentes")
    void dependenciaQuebradaDesabilitaFeaturesDependentes() throws Exception {
        String email = "dependency-capability@tenant.test";
        String token = registerAndLogin("Dependency Admin", email, "Tenant1234", "Capability Dependency");
        User user = userRepository.findByEmail(email).orElseThrow();

        var planId = insertPlan("DEPENDENCY");
        grantFeature(planId, "QUOTATION_WORKFLOW");
        grantFeature(planId, "CLIENT_PORTAL");
        insertSubscription(user.getTenant().getId(), planId, "ACTIVE");

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capabilities[0].available").value(false))
                .andExpect(jsonPath("$.capabilities[1].available").value(false))
                .andExpect(jsonPath("$.capabilities[2].available").value(false));
    }

    @Test
    @DisplayName("clientRecebe200EnquantoPlatformTokenESemTokenRecebem401")
    void clientRecebe200EnquantoPlatformTokenESemTokenRecebem401() throws Exception {
        String email = "client-capability-admin@tenant.test";
        String password = "Tenant1234";
        String token = registerAndLogin("Client Capability Admin", email, password, "Capability Client");
        User admin = userRepository.findByEmail(email).orElseThrow();
        Tenant tenant = admin.getTenant();

        var planId = insertPlan("CLIENT");
        grantFeature(planId, "COMMERCIAL_RFQ");
        grantFeature(planId, "QUOTATION_WORKFLOW");
        grantFeature(planId, "CLIENT_PORTAL");
        insertSubscription(tenant.getId(), planId, "ACTIVE");

        User client = createClientUser(tenant, "client-capability-user@tenant.test", password);
        String clientToken = login(client.getEmail(), password);

        createPlatformAdmin();
        String platformToken = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capabilities[0].key").value("CLIENT_PORTAL"));

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .header("Authorization", "Bearer " + platformToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/me/capabilities"))
                .andExpect(status().isUnauthorized());
    }
}
