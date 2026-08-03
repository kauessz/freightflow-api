package com.freightflow.modules.commercial.client;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.RfqRepository;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.customer.CustomerRepository;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.shared.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

abstract class AbstractClientPortalEntitlementIntegrationTest extends AbstractIntegrationTest {

    @Autowired protected TenantRepository tenantRepository;
    @Autowired protected UserRepository userRepository;
    @Autowired protected CustomerRepository customerRepository;
    @Autowired protected PortRepository portRepository;
    @Autowired protected RfqRepository rfqRepository;
    @Autowired protected QuotationRepository quotationRepository;
    @Autowired protected JdbcTemplate jdbcTemplate;

    protected static final Instant FIXTURE_TIME = Instant.parse("2026-08-03T12:00:00Z");

    protected Tenant createTenant(String suffix) {
        return tenantRepository.saveAndFlush(new Tenant(
                "Client Portal " + suffix,
                "client-portal-" + suffix.toLowerCase(),
                suffix.toLowerCase() + "@tenant.test",
                "FREE"
        ));
    }

    protected Customer createCustomer(Tenant tenant, String name) {
        return customerRepository.saveAndFlush(new Customer(tenant, name));
    }

    protected User createClientUser(Tenant tenant, Customer customer, String suffix) {
        User user = new User("Client " + suffix, "client-" + suffix.toLowerCase() + "@test.com", "hash", User.UserRole.CLIENT, tenant);
        user.setCustomer(customer);
        return userRepository.saveAndFlush(user);
    }

    protected User createAdminUser(Tenant tenant, String suffix) {
        return userRepository.saveAndFlush(new User("Admin " + suffix, "admin-" + suffix.toLowerCase() + "@test.com", "hash", User.UserRole.ADMIN, tenant));
    }

    protected Port createPort(String unlocode, String name, String country) {
        return portRepository.saveAndFlush(new Port(unlocode, name, country, "UTC", null, null));
    }

    protected RequestForQuotation createRfq(Tenant tenant,
                                            Customer customer,
                                            User createdBy,
                                            Port origin,
                                            Port destination,
                                            String reference,
                                            RfqStatus status) {
        RequestForQuotation rfq = new RequestForQuotation(
                tenant,
                reference,
                "Maria Portal",
                RfqDirection.EXPORT,
                RfqTransportMode.OCEAN,
                RfqServiceType.LCL,
                origin,
                destination,
                createdBy
        );
        rfq.setCustomer(customer);
        rfq.setContactEmail("maria.portal@test.com");
        rfq.setStatus(status);
        rfq.setProspectCompanyName(null);
        return rfqRepository.saveAndFlush(rfq);
    }

    protected Quotation createQuotation(Tenant tenant,
                                        RequestForQuotation rfq,
                                        User createdBy,
                                        String number,
                                        QuotationStatus status) {
        Quotation quotation = new Quotation(tenant, rfq, number, "USD", createdBy);
        quotation.setStatus(status);
        quotation.setCommercialNotes("Visible note");
        quotation.setCarrierName("Carrier " + number);
        quotation.setTotals(
                BigDecimal.ZERO,
                new BigDecimal("150.00"),
                new BigDecimal("150.00"),
                new BigDecimal("100.0000"),
                BigDecimal.ZERO
        );
        if (status == QuotationStatus.SENT) {
            quotation.setSentAt(FIXTURE_TIME);
            quotation.setSentBy(createdBy);
        }
        return quotationRepository.saveAndFlush(quotation);
    }

    protected UUID insertPlan(String codePrefix) {
        UUID planId = UUID.randomUUID();
        String code = (codePrefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8)).toUpperCase();
        Timestamp now = Timestamp.from(FIXTURE_TIME);
        jdbcTemplate.update("""
                insert into subscription_plans (id, code, name, description, status, display_order, custom, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                planId,
                code,
                code,
                "Client portal entitlement integration test plan",
                "ACTIVE",
                999,
                true,
                now,
                now
        );
        return planId;
    }

    protected void ensureFeature(String key, String name, String implementationStatus) {
        Timestamp now = Timestamp.from(FIXTURE_TIME);
        jdbcTemplate.update("""
                insert into platform_features (feature_key, name, description, value_type, unit, implementation_status, active, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (feature_key) do nothing
                """,
                key,
                name,
                "Client portal entitlement integration test feature",
                "BOOLEAN",
                null,
                implementationStatus,
                true,
                now,
                now
        );
    }

    protected void ensureDependency(String featureKey, String requiredFeatureKey) {
        jdbcTemplate.update("""
                insert into platform_feature_dependencies (feature_key, required_feature_key)
                values (?, ?)
                on conflict do nothing
                """,
                featureKey,
                requiredFeatureKey
        );
    }

    protected void grantPlanEntitlement(UUID planId, String featureKey, boolean enabled) {
        jdbcTemplate.update("""
                insert into plan_entitlements (plan_id, feature_key, enabled, limit_value)
                values (?, ?, ?, ?)
                """,
                planId,
                featureKey,
                enabled,
                null
        );
    }

    protected void insertSubscription(UUID tenantId, UUID planId, String status) {
        Timestamp now = Timestamp.from(FIXTURE_TIME);
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
                "fixture",
                null,
                now,
                now
        );
    }

    protected RequestPostProcessor asUser(User user) {
        return user(new UserPrincipal(
                user.getId(),
                user.getEmail(),
                null,
                user.getTenant().getId(),
                user.getRole().name(),
                user.getCustomer() != null ? user.getCustomer().getId() : null
        ));
    }
}
