package com.freightflow.modules.commercial.quotation;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.quotation.enums.ChargeCategory;
import com.freightflow.modules.commercial.quotation.enums.ChargeScope;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Quotation workflow entitlement enforcement integration")
@TestPropertySource(properties = "freightflow.entitlements.enforcement-mode=ENFORCE")
class QuotationWorkflowEntitlementEnforcementIntegrationTest extends AbstractIntegrationTest {

    private static final Instant FIXTURE_TIME = Instant.parse("2026-08-03T14:00:00Z");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PortRepository portRepository;

    @Autowired
    private RfqRepository rfqRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("tenantComCommercialRfqEQuotationWorkflowExecutaFluxoInternoAteSend")
    void tenantComCommercialRfqEQuotationWorkflowExecutaFluxoInternoAteSend() throws Exception {
        String token = registerAndLogin("Workflow Admin", "workflow-admin@test.com", "Tenant1234", "Workflow Tenant");
        User admin = userRepository.findByEmail("workflow-admin@test.com").orElseThrow();
        UUID planId = insertPlan("QUOTATION_WORKFLOW_ENABLED");
        grantEntitlement(planId, "COMMERCIAL_RFQ", true);
        grantEntitlement(planId, "QUOTATION_WORKFLOW", true);
        insertSubscription(admin.getTenant().getId(), planId, "ACTIVE");

        Customer customer = customerRepository.saveAndFlush(new Customer(admin.getTenant(), "Workflow Customer"));
        Port origin = portRepository.saveAndFlush(new Port("BRE3A", "Workflow Origin", "BR", "UTC", null, null));
        Port destination = portRepository.saveAndFlush(new Port("NLE3A", "Workflow Destination", "NL", "UTC", null, null));
        RequestForQuotation rfq = createUnderAnalysisRfq(admin, customer, origin, destination, "RFQ-WF-001");

        String createResponse = mockMvc.perform(post("/api/v1/commercial/rfqs/{rfqId}/quotations", rfq.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createQuotationBody("Q-WF-001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String quotationId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(get("/api/v1/commercial/quotations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].quotationNumber").value("Q-WF-001"));

        mockMvc.perform(get("/api/v1/commercial/quotations/{id}", quotationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotationNumber").value("Q-WF-001"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mockMvc.perform(post("/api/v1/commercial/quotations/{id}/items", quotationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));

        mockMvc.perform(post("/api/v1/commercial/quotations/{id}/ready-for-review", quotationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY_FOR_REVIEW"));

        RequestForQuotation rfqAfterReview = rfqRepository.findById(rfq.getId()).orElseThrow();
        assertThat(rfqAfterReview.getStatus()).isEqualTo(RfqStatus.UNDER_ANALYSIS);

        mockMvc.perform(post("/api/v1/commercial/quotations/{id}/approve", quotationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/api/v1/commercial/quotations/{id}/send", quotationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));

        Quotation persistedQuotation = quotationRepository.findByIdAndTenantId(UUID.fromString(quotationId), admin.getTenant().getId())
                .orElseThrow();
        RequestForQuotation persistedRfq = rfqRepository.findById(rfq.getId()).orElseThrow();

        assertThat(persistedQuotation.getStatus().name()).isEqualTo("SENT");
        assertThat(persistedRfq.getStatus()).isEqualTo(RfqStatus.QUOTED);
    }

    @Test
    @DisplayName("tenantSemSubscriptionRecebe403ESemPersistirQuotation")
    void tenantSemSubscriptionRecebe403ESemPersistirQuotation() throws Exception {
        String token = registerAndLogin("No Subscription", "quotation-no-sub@test.com", "Tenant1234", "Quotation No Sub");
        User admin = userRepository.findByEmail("quotation-no-sub@test.com").orElseThrow();
        Customer customer = customerRepository.saveAndFlush(new Customer(admin.getTenant(), "No Subscription Customer"));
        Port origin = portRepository.saveAndFlush(new Port("BRE3B", "No Sub Origin", "BR", "UTC", null, null));
        Port destination = portRepository.saveAndFlush(new Port("NLE3B", "No Sub Destination", "NL", "UTC", null, null));
        RequestForQuotation rfq = createUnderAnalysisRfq(admin, customer, origin, destination, "RFQ-WF-NOSUB");

        mockMvc.perform(post("/api/v1/commercial/rfqs/{rfqId}/quotations", rfq.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createQuotationBody("Q-WF-NOSUB")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("QUOTATION_WORKFLOW"));

        assertThat(quotationRepository.count()).isZero();
    }

    @Test
    @DisplayName("planoSemQuotationWorkflowRecebe403")
    void planoSemQuotationWorkflowRecebe403() throws Exception {
        String token = registerAndLogin("No Workflow", "quotation-no-workflow@test.com", "Tenant1234", "Quotation No Workflow");
        User admin = userRepository.findByEmail("quotation-no-workflow@test.com").orElseThrow();
        UUID planId = insertPlan("QUOTATION_WORKFLOW_MISSING");
        grantEntitlement(planId, "COMMERCIAL_RFQ", true);
        insertSubscription(admin.getTenant().getId(), planId, "ACTIVE");

        mockMvc.perform(get("/api/v1/commercial/quotations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("QUOTATION_WORKFLOW"));
    }

    @Test
    @DisplayName("quotationWorkflowSemCommercialRfqEfetivoRecebe403PelaDependencia")
    void quotationWorkflowSemCommercialRfqEfetivoRecebe403PelaDependencia() throws Exception {
        String token = registerAndLogin("Dependency Broken", "quotation-dependency@test.com", "Tenant1234", "Quotation Dependency");
        User admin = userRepository.findByEmail("quotation-dependency@test.com").orElseThrow();
        UUID planId = insertPlan("QUOTATION_WORKFLOW_WITHOUT_RFQ");
        grantEntitlement(planId, "QUOTATION_WORKFLOW", true);
        insertSubscription(admin.getTenant().getId(), planId, "ACTIVE");

        mockMvc.perform(get("/api/v1/commercial/quotations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("QUOTATION_WORKFLOW"));
    }

    @Test
    @DisplayName("subscriptionSuspendedBloqueiaWorkflowInternoSemPersistirQuotationNemAlterarRfq")
    void subscriptionSuspendedBloqueiaWorkflowInternoSemPersistirQuotationNemAlterarRfq() throws Exception {
        String token = registerAndLogin("Suspended Workflow", "quotation-suspended@test.com", "Tenant1234", "Quotation Suspended");
        User admin = userRepository.findByEmail("quotation-suspended@test.com").orElseThrow();
        UUID planId = insertPlan("QUOTATION_WORKFLOW_SUSPENDED");
        grantEntitlement(planId, "COMMERCIAL_RFQ", true);
        grantEntitlement(planId, "QUOTATION_WORKFLOW", true);
        insertSubscription(admin.getTenant().getId(), planId, "SUSPENDED");

        Customer customer = customerRepository.saveAndFlush(new Customer(admin.getTenant(), "Suspended Customer"));
        Port origin = portRepository.saveAndFlush(new Port("BRE3C", "Suspended Origin", "BR", "UTC", null, null));
        Port destination = portRepository.saveAndFlush(new Port("NLE3C", "Suspended Destination", "NL", "UTC", null, null));
        RequestForQuotation rfq = createUnderAnalysisRfq(admin, customer, origin, destination, "RFQ-WF-SUSPENDED");

        mockMvc.perform(get("/api/v1/commercial/quotations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Feature Not Available"))
                .andExpect(jsonPath("$.type").value("https://api.freightflow.com/errors/feature-not-available"))
                .andExpect(jsonPath("$.featureKey").value("QUOTATION_WORKFLOW"));

        assertThat(quotationRepository.count()).isZero();
        assertThat(rfqRepository.findById(rfq.getId()).orElseThrow().getStatus()).isEqualTo(RfqStatus.UNDER_ANALYSIS);
    }

    @Test
    @DisplayName("negacaoNaoAlteraQuotationNemRfqPreexistentesQuandoSubscriptionFicaSuspended")
    void negacaoNaoAlteraQuotationNemRfqPreexistentesQuandoSubscriptionFicaSuspended() throws Exception {
        String token = registerAndLogin("Workflow Existing", "quotation-existing@test.com", "Tenant1234", "Quotation Existing");
        User admin = userRepository.findByEmail("quotation-existing@test.com").orElseThrow();
        UUID planId = insertPlan("QUOTATION_WORKFLOW_EXISTING");
        grantEntitlement(planId, "COMMERCIAL_RFQ", true);
        grantEntitlement(planId, "QUOTATION_WORKFLOW", true);
        UUID subscriptionId = insertSubscription(admin.getTenant().getId(), planId, "ACTIVE");

        Customer customer = customerRepository.saveAndFlush(new Customer(admin.getTenant(), "Existing Customer"));
        Port origin = portRepository.saveAndFlush(new Port("BRE3D", "Existing Origin", "BR", "UTC", null, null));
        Port destination = portRepository.saveAndFlush(new Port("NLE3D", "Existing Destination", "NL", "UTC", null, null));
        RequestForQuotation rfq = createUnderAnalysisRfq(admin, customer, origin, destination, "RFQ-WF-EXISTING");
        Quotation quotation = createApprovedQuotationWithItem(admin, rfq, "Q-WF-EXISTING");

        BigDecimal expectedCostTotal = quotation.getCostTotal();
        BigDecimal expectedSellingTotal = quotation.getSellingTotal();
        BigDecimal expectedProfitAmount = quotation.getProfitAmount();
        BigDecimal expectedMargin = quotation.getMarginPercentage();
        BigDecimal expectedMarkup = quotation.getMarkupPercentage();
        int expectedItemCount = quotation.getItems().size();

        suspendSubscription(subscriptionId);

        mockMvc.perform(post("/api/v1/commercial/quotations/{id}/send", quotation.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("QUOTATION_WORKFLOW"));

        Quotation persistedQuotation = quotationRepository.findByIdAndTenantId(quotation.getId(), admin.getTenant().getId())
                .orElseThrow();
        RequestForQuotation persistedRfq = rfqRepository.findById(rfq.getId()).orElseThrow();

        assertThat(persistedQuotation.getStatus()).isEqualTo(QuotationStatus.APPROVED);
        assertThat(persistedQuotation.getSentAt()).isNull();
        assertThat(persistedQuotation.getSentBy()).isNull();
        Integer persistedItemCount = jdbcTemplate.queryForObject(
                "select count(*) from commercial_quotation_items where quotation_id = ?",
                Integer.class,
                quotation.getId()
        );
        assertThat(persistedItemCount).isEqualTo(expectedItemCount);
        assertThat(persistedQuotation.getCostTotal()).isEqualByComparingTo(expectedCostTotal);
        assertThat(persistedQuotation.getSellingTotal()).isEqualByComparingTo(expectedSellingTotal);
        assertThat(persistedQuotation.getProfitAmount()).isEqualByComparingTo(expectedProfitAmount);
        assertThat(persistedQuotation.getMarginPercentage()).isEqualByComparingTo(expectedMargin);
        assertThat(persistedQuotation.getMarkupPercentage()).isEqualByComparingTo(expectedMarkup);
        assertThat(persistedRfq.getStatus()).isEqualTo(RfqStatus.UNDER_ANALYSIS);
        assertThat(quotationRepository.count()).isEqualTo(1L);
    }

    private RequestForQuotation createUnderAnalysisRfq(User admin,
                                                       Customer customer,
                                                       Port origin,
                                                       Port destination,
                                                       String reference) {
        RequestForQuotation rfq = new RequestForQuotation(
                admin.getTenant(),
                reference,
                "Maria Workflow",
                RfqDirection.EXPORT,
                RfqTransportMode.OCEAN,
                RfqServiceType.FCL,
                origin,
                destination,
                admin
        );
        rfq.setCustomer(customer);
        rfq.setContactEmail("maria.workflow@test.com");
        rfq.setStatus(RfqStatus.UNDER_ANALYSIS);
        return rfqRepository.saveAndFlush(rfq);
    }

    private UUID insertPlan(String codePrefix) {
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
                "Quotation workflow integration test plan",
                "ACTIVE",
                998,
                true,
                now,
                now
        );
        return planId;
    }

    private void grantEntitlement(UUID planId, String featureKey, boolean enabled) {
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

    private UUID insertSubscription(UUID tenantId, UUID planId, String status) {
        UUID subscriptionId = UUID.randomUUID();
        Timestamp now = Timestamp.from(FIXTURE_TIME);
        jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                subscriptionId,
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
        return subscriptionId;
    }

    private void suspendSubscription(UUID subscriptionId) {
        jdbcTemplate.update("""
                update tenant_subscriptions
                   set status = ?, updated_at = ?
                 where id = ?
                """,
                "SUSPENDED",
                Timestamp.from(FIXTURE_TIME.plusSeconds(30)),
                subscriptionId
        );
    }

    private Quotation createApprovedQuotationWithItem(User admin, RequestForQuotation rfq, String quotationNumber) {
        Quotation quotation = new Quotation(admin.getTenant(), rfq, quotationNumber, "USD", admin);
        quotation.setCarrierName("MSC");
        quotation.setCommercialNotes("Pre-existing quotation");

        QuotationItem item = new QuotationItem(
                ChargeCategory.OCEAN_FREIGHT,
                "Ocean freight",
                ChargeScope.MAIN_CARRIAGE,
                "USD",
                new BigDecimal("100.00"),
                "USD",
                new BigDecimal("150.00"),
                BigDecimal.ONE,
                0
        );
        item.setCostAmountInSellingCurrency(new BigDecimal("100.00"));
        item.setTotals(
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                new BigDecimal("50.00"),
                new BigDecimal("33.3333"),
                new BigDecimal("50.0000")
        );
        quotation.addItem(item);
        quotation.setTotals(
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                new BigDecimal("50.00"),
                new BigDecimal("33.3333"),
                new BigDecimal("50.0000")
        );
        quotation.setStatus(QuotationStatus.APPROVED);
        quotation.setApprovedAt(FIXTURE_TIME);
        quotation.setApprovedBy(admin);
        return quotationRepository.saveAndFlush(quotation);
    }

    private String createQuotationBody(String quotationNumber) {
        return """
                {
                  "quotationNumber": "%s",
                  "validUntil": "2026-09-10T10:00:00Z",
                  "carrierName": "MSC",
                  "transitTimeDays": 18,
                  "freeTimeDays": 7,
                  "estimatedDeparture": "2026-09-01T10:00:00Z",
                  "estimatedArrival": "2026-09-20T10:00:00Z",
                  "sellingCurrency": "USD",
                  "commercialNotes": "Workflow quotation"
                }
                """.formatted(quotationNumber);
    }

    private String addItemBody() {
        return """
                {
                  "category": "OCEAN_FREIGHT",
                  "description": "Ocean freight",
                  "scope": "MAIN_CARRIAGE",
                  "costCurrency": "USD",
                  "costAmount": 100,
                  "sellingCurrency": "USD",
                  "sellingAmount": 150,
                  "quantity": 1,
                  "included": true,
                  "optional": false,
                  "sortOrder": 0
                }
                """;
    }
}
