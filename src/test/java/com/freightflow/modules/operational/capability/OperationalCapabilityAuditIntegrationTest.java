package com.freightflow.modules.operational.capability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.freightflow.modules.platform.entitlement.EntitlementEnforcementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Operational capability audit integration")
@TestPropertySource(properties = "freightflow.entitlements.enforcement-mode=AUDIT")
class OperationalCapabilityAuditIntegrationTest extends OperationalCapabilityIntegrationSupport {

    @BeforeEach
    void setUpFixture() {
        ensureOperationalCapabilityCatalog();
    }

    @Test
    @DisplayName("tenantSemSubscriptionRecebeTresTrueSemLogAuditFalso")
    void tenantSemSubscriptionRecebeTresTrueSemLogAuditFalso() throws Exception {
        String token = registerAndLogin("Audit Admin", "audit-capability@tenant.test", "Tenant1234", "Capability Audit");
        Integer beforeSubscriptions = jdbcTemplate.queryForObject("select count(*) from tenant_subscriptions", Integer.class);

        Logger logger = (Logger) LoggerFactory.getLogger(EntitlementEnforcementService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            mockMvc.perform(get("/api/v1/me/capabilities")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.capabilities[0].available").value(true))
                    .andExpect(jsonPath("$.capabilities[1].available").value(true))
                    .andExpect(jsonPath("$.capabilities[2].available").value(true));

            Integer afterSubscriptions = jdbcTemplate.queryForObject("select count(*) from tenant_subscriptions", Integer.class);
            assertThat(afterSubscriptions).isEqualTo(beforeSubscriptions);
            assertThat(appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .filter(message -> message.contains("Entitlement audit deny candidate")))
                    .isEmpty();
        } finally {
            logger.detachAppender(appender);
        }
    }
}
