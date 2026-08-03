package com.freightflow.modules.commercial.client;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.port.Port;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Client portal entitlement enforcement integration")
@TestPropertySource(properties = "freightflow.entitlements.enforcement-mode=ENFORCE")
class ClientPortalEntitlementEnforcementIntegrationTest extends AbstractClientPortalEntitlementIntegrationTest {

    private Tenant enabledTenant;
    private Customer enabledCustomerA;
    private Customer enabledCustomerB;
    private User enabledClientA;
    private User enabledClientB;
    private User enabledAdmin;
    private RequestForQuotation enabledRfqA;
    private Quotation enabledSentQuotationA;
    private Quotation enabledApprovedQuotationA;

    private Tenant noSubscriptionTenant;
    private Customer noSubscriptionCustomer;
    private User noSubscriptionClient;

    private Tenant suspendedTenant;
    private Customer suspendedCustomer;
    private User suspendedClient;

    private Tenant noPortalTenant;
    private Customer noPortalCustomer;
    private User noPortalClient;
    private Quotation noPortalSentQuotation;

    private Tenant noWorkflowTenant;
    private Customer noWorkflowCustomer;
    private User noWorkflowClient;

    private Tenant noPortalNoWorkflowTenant;
    private Customer noPortalNoWorkflowCustomer;
    private User noPortalNoWorkflowClient;
    private Quotation noPortalNoWorkflowSentQuotation;

    private Tenant workflowWithoutRfqTenant;
    private Customer workflowWithoutRfqCustomer;
    private User workflowWithoutRfqClient;

    private Tenant otherEnabledTenant;
    private Customer otherEnabledCustomer;
    private User otherEnabledClient;
    private RequestForQuotation otherTenantRfq;
    private Quotation otherTenantSentQuotation;

    @BeforeEach
    void setUpData() {
        ensureFeature("COMMERCIAL_RFQ", "Commercial RFQ", "AVAILABLE");
        ensureFeature("CLIENT_PORTAL", "Client Portal", "PARTIAL");
        ensureDependency("CLIENT_PORTAL", "COMMERCIAL_RFQ");

        Port origin = createPort("BRE2A", "Fixture Origin", "BR");
        Port destination = createPort("NLE2A", "Fixture Destination", "NL");

        enabledTenant = createTenant("Enabled");
        enabledCustomerA = createCustomer(enabledTenant, "Atlas Cargo");
        enabledCustomerB = createCustomer(enabledTenant, "Meridian Imports");
        enabledClientA = createClientUser(enabledTenant, enabledCustomerA, "EnabledA");
        enabledClientB = createClientUser(enabledTenant, enabledCustomerB, "EnabledB");
        enabledAdmin = createAdminUser(enabledTenant, "Enabled");
        UUID enabledPlanId = insertPlan("CLIENT_PORTAL_ENABLED");
        grantPlanEntitlement(enabledPlanId, "COMMERCIAL_RFQ", true);
        grantPlanEntitlement(enabledPlanId, "QUOTATION_WORKFLOW", true);
        grantPlanEntitlement(enabledPlanId, "CLIENT_PORTAL", true);
        insertSubscription(enabledTenant.getId(), enabledPlanId, "ACTIVE");
        enabledRfqA = createRfq(enabledTenant, enabledCustomerA, enabledClientA, origin, destination, "RFQ-CLIENT-ENABLED-1", RfqStatus.DRAFT);
        enabledSentQuotationA = createQuotation(enabledTenant, enabledRfqA, enabledAdmin, "Q-CLIENT-SENT-1", QuotationStatus.SENT);
        enabledApprovedQuotationA = createQuotation(enabledTenant, enabledRfqA, enabledAdmin, "Q-CLIENT-APPROVED-1", QuotationStatus.APPROVED);

        noSubscriptionTenant = createTenant("NoSub");
        noSubscriptionCustomer = createCustomer(noSubscriptionTenant, "No Subscription Customer");
        noSubscriptionClient = createClientUser(noSubscriptionTenant, noSubscriptionCustomer, "NoSub");

        suspendedTenant = createTenant("Suspended");
        suspendedCustomer = createCustomer(suspendedTenant, "Suspended Customer");
        suspendedClient = createClientUser(suspendedTenant, suspendedCustomer, "Suspended");
        UUID suspendedPlanId = insertPlan("CLIENT_PORTAL_SUSPENDED");
        grantPlanEntitlement(suspendedPlanId, "COMMERCIAL_RFQ", true);
        grantPlanEntitlement(suspendedPlanId, "QUOTATION_WORKFLOW", true);
        grantPlanEntitlement(suspendedPlanId, "CLIENT_PORTAL", true);
        insertSubscription(suspendedTenant.getId(), suspendedPlanId, "SUSPENDED");

        noPortalTenant = createTenant("NoPortal");
        noPortalCustomer = createCustomer(noPortalTenant, "No Portal Customer");
        noPortalClient = createClientUser(noPortalTenant, noPortalCustomer, "NoPortal");
        UUID noPortalPlanId = insertPlan("CLIENT_PORTAL_NO_PORTAL");
        grantPlanEntitlement(noPortalPlanId, "COMMERCIAL_RFQ", true);
        grantPlanEntitlement(noPortalPlanId, "QUOTATION_WORKFLOW", true);
        insertSubscription(noPortalTenant.getId(), noPortalPlanId, "ACTIVE");
        User noPortalAdmin = createAdminUser(noPortalTenant, "NoPortal");
        RequestForQuotation noPortalRfq = createRfq(noPortalTenant, noPortalCustomer, noPortalClient, origin, destination, "RFQ-CLIENT-NO-PORTAL", RfqStatus.DRAFT);
        noPortalSentQuotation = createQuotation(noPortalTenant, noPortalRfq, noPortalAdmin, "Q-CLIENT-NO-PORTAL", QuotationStatus.SENT);

        noWorkflowTenant = createTenant("NoWorkflow");
        noWorkflowCustomer = createCustomer(noWorkflowTenant, "No Workflow Customer");
        noWorkflowClient = createClientUser(noWorkflowTenant, noWorkflowCustomer, "NoWorkflow");
        UUID noWorkflowPlanId = insertPlan("CLIENT_PORTAL_NO_WORKFLOW");
        grantPlanEntitlement(noWorkflowPlanId, "COMMERCIAL_RFQ", true);
        grantPlanEntitlement(noWorkflowPlanId, "CLIENT_PORTAL", true);
        insertSubscription(noWorkflowTenant.getId(), noWorkflowPlanId, "ACTIVE");

        noPortalNoWorkflowTenant = createTenant("NoPortalNoWorkflow");
        noPortalNoWorkflowCustomer = createCustomer(noPortalNoWorkflowTenant, "No Portal No Workflow Customer");
        noPortalNoWorkflowClient = createClientUser(noPortalNoWorkflowTenant, noPortalNoWorkflowCustomer, "NoPortalNoWorkflow");
        UUID noPortalNoWorkflowPlanId = insertPlan("CLIENT_PORTAL_NO_PORTAL_NO_WORKFLOW");
        insertSubscription(noPortalNoWorkflowTenant.getId(), noPortalNoWorkflowPlanId, "ACTIVE");
        User noPortalNoWorkflowAdmin = createAdminUser(noPortalNoWorkflowTenant, "NoPortalNoWorkflow");
        RequestForQuotation noPortalNoWorkflowRfq = createRfq(noPortalNoWorkflowTenant, noPortalNoWorkflowCustomer, noPortalNoWorkflowClient, origin, destination, "RFQ-CLIENT-NO-PORTAL-NO-WORKFLOW", RfqStatus.DRAFT);
        noPortalNoWorkflowSentQuotation = createQuotation(noPortalNoWorkflowTenant, noPortalNoWorkflowRfq, noPortalNoWorkflowAdmin, "Q-CLIENT-NO-PORTAL-NO-WORKFLOW", QuotationStatus.SENT);

        workflowWithoutRfqTenant = createTenant("WorkflowWithoutRfq");
        workflowWithoutRfqCustomer = createCustomer(workflowWithoutRfqTenant, "Workflow Without RFQ Customer");
        workflowWithoutRfqClient = createClientUser(workflowWithoutRfqTenant, workflowWithoutRfqCustomer, "WorkflowWithoutRfq");
        UUID workflowWithoutRfqPlanId = insertPlan("CLIENT_PORTAL_WITHOUT_RFQ");
        grantPlanEntitlement(workflowWithoutRfqPlanId, "CLIENT_PORTAL", true);
        grantPlanEntitlement(workflowWithoutRfqPlanId, "QUOTATION_WORKFLOW", true);
        insertSubscription(workflowWithoutRfqTenant.getId(), workflowWithoutRfqPlanId, "ACTIVE");

        otherEnabledTenant = createTenant("OtherEnabled");
        otherEnabledCustomer = createCustomer(otherEnabledTenant, "Other Tenant Customer");
        otherEnabledClient = createClientUser(otherEnabledTenant, otherEnabledCustomer, "OtherEnabled");
        User otherAdmin = createAdminUser(otherEnabledTenant, "OtherEnabled");
        UUID otherEnabledPlanId = insertPlan("CLIENT_PORTAL_OTHER_ENABLED");
        grantPlanEntitlement(otherEnabledPlanId, "COMMERCIAL_RFQ", true);
        grantPlanEntitlement(otherEnabledPlanId, "QUOTATION_WORKFLOW", true);
        grantPlanEntitlement(otherEnabledPlanId, "CLIENT_PORTAL", true);
        insertSubscription(otherEnabledTenant.getId(), otherEnabledPlanId, "ACTIVE");
        otherTenantRfq = createRfq(otherEnabledTenant, otherEnabledCustomer, otherEnabledClient, origin, destination, "RFQ-CLIENT-OTHER-TENANT", RfqStatus.DRAFT);
        otherTenantSentQuotation = createQuotation(otherEnabledTenant, otherTenantRfq, otherAdmin, "Q-CLIENT-OTHER-TENANT", QuotationStatus.SENT);
    }

    @Test
    @DisplayName("activeComCommercialRfqEClientPortalPodeCriarListarConsultarAtualizarSubmeterECancelar")
    void activeComCommercialRfqEClientPortalPodeCriarListarConsultarAtualizarSubmeterECancelar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/client/rfqs")
                        .with(asUser(enabledClientA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody("RFQ-CLIENT-NEW-1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("RFQ-CLIENT-NEW-1"))
                .andExpect(jsonPath("$.tenantId").doesNotExist())
                .andExpect(jsonPath("$.customerId").doesNotExist());

        UUID createdRfqId = rfqRepository.findByTenantIdAndCustomerId(enabledTenant.getId(), enabledCustomerA.getId(), org.springframework.data.domain.PageRequest.of(0, 20))
                .getContent()
                .stream()
                .filter(item -> item.getReference().equals("RFQ-CLIENT-NEW-1"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Created RFQ fixture was not found"))
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs")
                        .with(asUser(enabledClientA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs/{id}", enabledRfqA.getId())
                        .with(asUser(enabledClientA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(enabledRfqA.getId().toString()))
                .andExpect(jsonPath("$.quotationCount").value(2));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/client/rfqs/{id}", createdRfqId)
                        .with(asUser(enabledClientA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateBody("RFQ-CLIENT-UPDATED-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value("RFQ-CLIENT-UPDATED-1"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/client/rfqs/{id}/submit", createdRfqId)
                        .with(asUser(enabledClientA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/client/rfqs/{id}/cancel", createdRfqId)
                        .with(asUser(enabledClientA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        RequestForQuotation persisted = rfqRepository.findByIdAndTenantIdAndCustomerId(createdRfqId, enabledTenant.getId(), enabledCustomerA.getId())
                .orElseThrow(() -> new AssertionError("Created RFQ not found in enabled tenant scope"));
        assertThat(persisted.getTenant().getId()).isEqualTo(enabledTenant.getId());
        assertThat(persisted.getCustomer().getId()).isEqualTo(enabledCustomerA.getId());
        assertThat(persisted.getStatus()).isEqualTo(RfqStatus.CANCELLED);
    }

    @Test
    @DisplayName("tenantSemSubscriptionRecebe403ESemPersistenciaMesmoComOutroTenantHabilitado")
    void tenantSemSubscriptionRecebe403ESemPersistenciaMesmoComOutroTenantHabilitado() throws Exception {
        long before = rfqRepository.countByTenantId(noSubscriptionTenant.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/client/rfqs")
                        .with(asUser(noSubscriptionClient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody("RFQ-NO-SUB-1")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Feature Not Available"))
                .andExpect(jsonPath("$.featureKey").value("CLIENT_PORTAL"));

        assertThat(rfqRepository.countByTenantId(noSubscriptionTenant.getId())).isEqualTo(before);
    }

    @Test
    @DisplayName("subscriptionSuspendedRecebe403")
    void subscriptionSuspendedRecebe403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs")
                        .with(asUser(suspendedClient)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("CLIENT_PORTAL"));
    }

    @Test
    @DisplayName("planoSemClientPortalRecebe403")
    void planoSemClientPortalRecebe403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs")
                        .with(asUser(noPortalClient)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("CLIENT_PORTAL"));
    }

    @Test
    @DisplayName("quotationClientSemClientPortalRecebe403MesmoQuandoWorkflowEstaEfetivo")
    void quotationClientSemClientPortalRecebe403MesmoQuandoWorkflowEstaEfetivo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations/{id}", noPortalSentQuotation.getId())
                        .with(asUser(noPortalClient)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("CLIENT_PORTAL"));
    }

    @Test
    @DisplayName("clientPortalSemQuotationWorkflowRecebe403")
    void clientPortalSemQuotationWorkflowRecebe403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations")
                        .with(asUser(noWorkflowClient)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("QUOTATION_WORKFLOW"));
    }

    @Test
    @DisplayName("quotationClientSemClientPortalESemQuotationWorkflowRecebe403ComPrimeiraNegacaoDeterministica")
    void quotationClientSemClientPortalESemQuotationWorkflowRecebe403ComPrimeiraNegacaoDeterministica() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations/{id}", noPortalNoWorkflowSentQuotation.getId())
                        .with(asUser(noPortalNoWorkflowClient)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("CLIENT_PORTAL"));
    }

    @Test
    @DisplayName("quotationWorkflowSemCommercialRfqEfetivoRecebe403PelaDependencia")
    void quotationWorkflowSemCommercialRfqEfetivoRecebe403PelaDependencia() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations")
                        .with(asUser(workflowWithoutRfqClient)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.featureKey").value("CLIENT_PORTAL"));
    }

    @Test
    @DisplayName("quotationSentDoCustomerCorretoFicaVisivelSemVazarCamposInternos")
    void quotationSentDoCustomerCorretoFicaVisivelSemVazarCamposInternos() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations")
                        .with(asUser(enabledClientA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].quotationNumber").value("Q-CLIENT-SENT-1"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations/{id}", enabledSentQuotationA.getId())
                        .with(asUser(enabledClientA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotationNumber").value("Q-CLIENT-SENT-1"))
                .andExpect(jsonPath("$.tenantId").doesNotExist())
                .andExpect(jsonPath("$.customerId").doesNotExist())
                .andExpect(jsonPath("$.costCurrency").doesNotExist())
                .andExpect(jsonPath("$.costAmount").doesNotExist())
                .andExpect(jsonPath("$.costTotal").doesNotExist())
                .andExpect(jsonPath("$.totalCost").doesNotExist())
                .andExpect(jsonPath("$.profitAmount").doesNotExist())
                .andExpect(jsonPath("$.marginPercentage").doesNotExist())
                .andExpect(jsonPath("$.markupPercentage").doesNotExist())
                .andExpect(jsonPath("$.supplierName").doesNotExist())
                .andExpect(jsonPath("$.internalNotes").doesNotExist())
                .andExpect(jsonPath("$.approvedBy").doesNotExist())
                .andExpect(jsonPath("$.sentBy").doesNotExist());
    }

    @Test
    @DisplayName("quotationNaoSentContinuaInvisivelQuandoEntitlementPermite")
    void quotationNaoSentContinuaInvisivelQuandoEntitlementPermite() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations/{id}", enabledApprovedQuotationA.getId())
                        .with(asUser(enabledClientA)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("crossCustomerMesmoTenantPermanece404")
    void crossCustomerMesmoTenantPermanece404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs/{id}", enabledRfqA.getId())
                        .with(asUser(enabledClientB)))
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations/{id}", enabledSentQuotationA.getId())
                        .with(asUser(enabledClientB)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("crossTenantPermanece404")
    void crossTenantPermanece404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs/{id}", otherTenantRfq.getId())
                        .with(asUser(enabledClientA)))
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations/{id}", otherTenantSentQuotation.getId())
                        .with(asUser(enabledClientA)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("usuarioNaoClientMantem403")
    void usuarioNaoClientMantem403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs")
                        .with(asUser(enabledAdmin)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("semTokenMantem401")
    void semTokenMantem401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs"))
                .andExpect(status().isUnauthorized());
    }

    private String validCreateBody(String reference) {
        return """
                {
                  "reference": "%s",
                  "contactName": "Maria Portal",
                  "contactEmail": "maria.portal@test.com",
                  "direction": "EXPORT",
                  "transportMode": "OCEAN",
                  "serviceType": "FCL",
                  "originPortId": "%s",
                  "destinationPortId": "%s",
                  "cargoItems": [
                    {
                      "description": "Electronics",
                      "packageType": "PALLET",
                      "packageQuantity": 10,
                      "grossWeight": 1200,
                      "weightUnit": "KG",
                      "volume": 12.5,
                      "volumeUnit": "CBM",
                      "stackable": true
                    }
                  ],
                  "containers": [
                    {
                      "containerType": "DRY_20",
                      "quantity": 1,
                      "weightPerContainer": 1200,
                      "weightUnit": "KG"
                    }
                  ]
                }
                """.formatted(
                reference,
                enabledRfqA.getOriginPort().getId(),
                enabledRfqA.getDestinationPort().getId()
        );
    }

    private String validUpdateBody(String reference) {
        return """
                {
                  "reference": "%s",
                  "notes": "Updated by client portal"
                }
                """.formatted(reference);
    }
}
