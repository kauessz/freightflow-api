package com.freightflow.modules.operational.capability;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.customer.CustomerRepository;
import com.freightflow.modules.platform.PlatformRole;
import com.freightflow.modules.platform.PlatformUser;
import com.freightflow.modules.platform.PlatformUserRepository;
import com.freightflow.modules.platform.PlatformUserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

abstract class OperationalCapabilityIntegrationSupport extends AbstractIntegrationTest {

    @Autowired
    protected TenantRepository tenantRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CustomerRepository customerRepository;

    @Autowired
    protected PlatformUserRepository platformUserRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected void ensureOperationalCapabilityCatalog() {
        Timestamp now = Timestamp.from(Instant.parse("2026-08-03T12:00:00Z"));
        upsertFeature("COMMERCIAL_RFQ", "Commercial RFQ", "AVAILABLE", now);
        upsertFeature("QUOTATION_WORKFLOW", "Quotation Workflow", "AVAILABLE", now);
        upsertFeature("CLIENT_PORTAL", "Client Portal", "PARTIAL", now);

        jdbcTemplate.update("""
                insert into platform_feature_dependencies (feature_key, required_feature_key)
                values (?, ?)
                on conflict (feature_key, required_feature_key) do nothing
                """,
                "QUOTATION_WORKFLOW",
                "COMMERCIAL_RFQ"
        );
        jdbcTemplate.update("""
                insert into platform_feature_dependencies (feature_key, required_feature_key)
                values (?, ?)
                on conflict (feature_key, required_feature_key) do nothing
                """,
                "CLIENT_PORTAL",
                "COMMERCIAL_RFQ"
        );
    }

    protected Tenant createTenant(String suffix) {
        return tenantRepository.saveAndFlush(new Tenant(
                "Capability Tenant " + suffix,
                "capability-" + suffix.toLowerCase(),
                suffix.toLowerCase() + "@tenant.test",
                "FREE"
        ));
    }

    protected User createUser(Tenant tenant, String email, String password, User.UserRole role) {
        User user = new User(email, email, passwordEncoder.encode(password), role, tenant);
        return userRepository.saveAndFlush(user);
    }

    protected User createClientUser(Tenant tenant, String email, String password) {
        Customer customer = customerRepository.saveAndFlush(new Customer(tenant, "Customer " + email));
        User user = new User(email, email, passwordEncoder.encode(password), User.UserRole.CLIENT, tenant);
        user.setCustomer(customer);
        return userRepository.saveAndFlush(user);
    }

    protected UUID insertPlan(String codeSuffix) {
        UUID planId = UUID.randomUUID();
        Timestamp now = Timestamp.from(Instant.parse("2026-08-03T12:00:00Z"));
        jdbcTemplate.update("""
                insert into subscription_plans (id, code, name, description, status, display_order, custom, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                planId,
                ("CAP_" + codeSuffix).toUpperCase(),
                "Capability " + codeSuffix,
                "Operational capability integration plan",
                "ACTIVE",
                999,
                true,
                now,
                now
        );
        return planId;
    }

    protected void grantFeature(UUID planId, String featureKey) {
        jdbcTemplate.update("""
                insert into plan_entitlements (plan_id, feature_key, enabled, limit_value)
                values (?, ?, ?, ?)
                """,
                planId,
                featureKey,
                true,
                null
        );
    }

    protected void insertSubscription(UUID tenantId, UUID planId, String status) {
        Timestamp now = Timestamp.from(Instant.parse("2026-08-03T12:00:00Z"));
        jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                tenantId,
                planId,
                status,
                now,
                null,
                "operational-capability-test",
                null,
                now,
                now
        );
    }

    protected String platformLogin(String email, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/platform/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = read(responseBody, "$.accessToken");
        assertThat(token).isNotBlank();
        return token;
    }

    protected void createPlatformAdmin() {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
    }

    private void upsertFeature(String key, String name, String implementationStatus, Timestamp now) {
        jdbcTemplate.update("""
                insert into platform_features (feature_key, name, description, value_type, unit, implementation_status, active, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (feature_key) do update set
                    name = excluded.name,
                    description = excluded.description,
                    value_type = excluded.value_type,
                    unit = excluded.unit,
                    implementation_status = excluded.implementation_status,
                    active = excluded.active,
                    updated_at = excluded.updated_at
                """,
                key,
                name,
                "Operational capability coverage test fixture for " + key,
                "BOOLEAN",
                null,
                implementationStatus,
                true,
                now,
                now
        );
    }
}
