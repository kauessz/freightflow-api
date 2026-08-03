package com.freightflow.modules.platform.entitlement;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.rfq.RfqRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Entitlement enforcement integration")
@TestPropertySource(properties = "freightflow.entitlements.enforcement-mode=ENFORCE")
class EntitlementEnforcementIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RfqRepository rfqRepository;

    private UUID originPortId;
    private UUID destinationPortId;

    @BeforeEach
    void setUpFixture() {
        ensureCommercialRfqFeatureExists();
        originPortId = insertPort("BRE1A", "Entitlement Origin Port");
        destinationPortId = insertPort("NLE1A", "Entitlement Destination Port");
    }

    @Test
    @DisplayName("tenantSemSubscriptionRecebe403ESemSideEffect")
    void tenantSemSubscriptionRecebe403ESemSideEffect() throws Exception {
        String token = registerAndLogin("No Subscription", "nosub@tenant.com", "Tenant1234", "Tenant No Sub");

        mockMvc.perform(post("/api/v1/commercial/rfqs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Feature Not Available"))
                .andExpect(jsonPath("$.type").value("https://api.freightflow.com/errors/feature-not-available"))
                .andExpect(jsonPath("$.featureKey").value("COMMERCIAL_RFQ"));

        assertThat(rfqRepository.count()).isZero();
        Integer commercialRows = jdbcTemplate.queryForObject("select count(*) from commercial_rfqs", Integer.class);
        assertThat(commercialRows).isZero();
    }

    @Test
    @DisplayName("tenantComProfessionalAtivoPodeListarECriarRfq")
    void tenantComProfessionalAtivoPodeListarECriarRfq() throws Exception {
        String token = registerAndLogin("Enabled User", "enabled@tenant.com", "Tenant1234", "Tenant Enabled");
        User user = userRepository.findByEmail("enabled@tenant.com").orElseThrow();
        UUID professionalPlanId = insertActivePlan("PROFESSIONAL_E1");
        grantCommercialRfq(professionalPlanId);
        insertSubscription(user.getTenant().getId(), professionalPlanId, "ACTIVE");

        mockMvc.perform(get("/api/v1/commercial/rfqs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/commercial/rfqs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("RFQ-E1-001"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        assertThat(rfqRepository.count()).isEqualTo(1);
        assertThat(rfqRepository.countByTenantId(user.getTenant().getId())).isEqualTo(1);
        UUID persistedTenantId = jdbcTemplate.queryForObject(
                "select tenant_id from commercial_rfqs where reference = ?",
                UUID.class,
                "RFQ-E1-001"
        );
        assertThat(persistedTenantId).isEqualTo(user.getTenant().getId());
    }

    @Test
    @DisplayName("subscriptionSuspendedRecebe403")
    void subscriptionSuspendedRecebe403() throws Exception {
        String token = registerAndLogin("Suspended User", "suspended@tenant.com", "Tenant1234", "Tenant Suspended");
        User user = userRepository.findByEmail("suspended@tenant.com").orElseThrow();
        UUID professionalPlanId = insertActivePlan("PROFESSIONAL_E1_SUSPENDED");
        grantCommercialRfq(professionalPlanId);
        insertSubscription(user.getTenant().getId(), professionalPlanId, "SUSPENDED");

        mockMvc.perform(get("/api/v1/commercial/rfqs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("COMMERCIAL_RFQ"));
    }

    @Test
    @DisplayName("planoSemCommercialRfqRecebe403")
    void planoSemCommercialRfqRecebe403() throws Exception {
        String token = registerAndLogin("No Feature User", "no-feature@tenant.com", "Tenant1234", "Tenant No Feature");
        User user = userRepository.findByEmail("no-feature@tenant.com").orElseThrow();
        UUID noRfqPlanId = insertActivePlan("STARTER_E1_NO_RFQ");
        insertSubscription(user.getTenant().getId(), noRfqPlanId, "ACTIVE");

        mockMvc.perform(get("/api/v1/commercial/rfqs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("COMMERCIAL_RFQ"));
    }

    @Test
    @DisplayName("decisaoPermaneceIsoladaPorTenant")
    void decisaoPermaneceIsoladaPorTenant() throws Exception {
        String allowedToken = registerAndLogin("Allowed User", "allowed@tenant.com", "Tenant1234", "Tenant Allowed");
        String deniedToken = registerAndLogin("Denied User", "denied@tenant.com", "Tenant1234", "Tenant Denied");

        User allowedUser = userRepository.findByEmail("allowed@tenant.com").orElseThrow();
        UUID professionalPlanId = insertActivePlan("PROFESSIONAL_E1_SCOPED");
        grantCommercialRfq(professionalPlanId);
        insertSubscription(allowedUser.getTenant().getId(), professionalPlanId, "ACTIVE");

        mockMvc.perform(get("/api/v1/commercial/rfqs")
                        .header("Authorization", "Bearer " + allowedToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/commercial/rfqs")
                        .header("Authorization", "Bearer " + deniedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("COMMERCIAL_RFQ"));
    }

    private void ensureCommercialRfqFeatureExists() {
        Timestamp now = Timestamp.from(Instant.parse("2026-08-02T13:00:00Z"));
        jdbcTemplate.update("""
                insert into platform_features (feature_key, name, description, value_type, unit, implementation_status, active, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (feature_key) do nothing
                """,
                "COMMERCIAL_RFQ",
                "Commercial RFQ",
                "Internal request-for-quotation workflow for logistics deals.",
                "BOOLEAN",
                null,
                "AVAILABLE",
                true,
                now,
                now
        );
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from platform_features where feature_key = ?",
                Integer.class,
                "COMMERCIAL_RFQ"
        );
        assertThat(count).isEqualTo(1);
    }

    private UUID insertPort(String unlocode, String name) {
        UUID portId = UUID.randomUUID();
        Timestamp now = Timestamp.from(Instant.parse("2026-08-02T13:00:00Z"));
        jdbcTemplate.update("""
                insert into ports (id, unlocode, name, country, timezone, latitude, longitude, active, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                portId,
                unlocode,
                name,
                unlocode.substring(0, 2),
                "UTC",
                null,
                null,
                true,
                now,
                now
        );
        return portId;
    }

    private UUID insertActivePlan(String codePrefix) {
        UUID planId = UUID.randomUUID();
        String code = (codePrefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8)).toUpperCase();
        Timestamp now = Timestamp.from(Instant.parse("2026-08-02T13:00:00Z"));
        jdbcTemplate.update("""
                insert into subscription_plans (id, code, name, description, status, display_order, custom, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                planId,
                code,
                code,
                "Integration-test plan for entitlement enforcement",
                "ACTIVE",
                999,
                true,
                now,
                now
        );
        return planId;
    }

    private void grantCommercialRfq(UUID planId) {
        jdbcTemplate.update("""
                insert into plan_entitlements (plan_id, feature_key, enabled, limit_value)
                values (?, ?, ?, ?)
                """,
                planId,
                "COMMERCIAL_RFQ",
                true,
                null
        );
    }

    private void insertSubscription(UUID tenantId, UUID planId, String status) {
        Timestamp now = Timestamp.from(Instant.parse("2026-08-02T13:00:00Z"));
        jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), tenantId, planId, status, now, null, "seed", null, now, now
        );
    }

    private String validCreateBody() {
        return """
                {
                  "reference": "RFQ-E1-001",
                  "prospectCompanyName": "Prospect Ocean",
                  "contactName": "Maria",
                  "contactEmail": "maria@test.com",
                  "direction": "EXPORT",
                  "transportMode": "OCEAN",
                  "serviceType": "LCL",
                  "originPortId": "%s",
                  "destinationPortId": "%s",
                  "cargoItems": [
                    {
                      "description": "Electronics",
                      "packageQuantity": 1,
                      "grossWeight": 100,
                      "weightUnit": "KG"
                    }
                  ]
                }
                """.formatted(originPortId, destinationPortId);
    }
}
