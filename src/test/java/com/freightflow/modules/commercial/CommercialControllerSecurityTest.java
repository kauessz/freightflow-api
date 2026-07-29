package com.freightflow.modules.commercial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.modules.commercial.client.quotation.ClientQuotationController;
import com.freightflow.modules.commercial.client.quotation.ClientQuotationService;
import com.freightflow.modules.commercial.client.rfq.ClientRfqController;
import com.freightflow.modules.commercial.client.rfq.ClientRfqService;
import com.freightflow.modules.commercial.quotation.QuotationController;
import com.freightflow.modules.commercial.quotation.QuotationService;
import com.freightflow.modules.commercial.rfq.RfqController;
import com.freightflow.modules.commercial.rfq.RfqService;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.rbac.RoleCheckAspect;
import com.freightflow.shared.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        RfqController.class,
        QuotationController.class,
        ClientRfqController.class,
        ClientQuotationController.class
})
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, CommercialControllerSecurityTest.RoleAspectTestConfig.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("Commercial controller security")
class CommercialControllerSecurityTest {

    @TestConfiguration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class RoleAspectTestConfig {
        @Bean
        RoleCheckAspect roleCheckAspect() {
            return new RoleCheckAspect();
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RfqService rfqService;
    @MockBean private QuotationService quotationService;
    @MockBean private ClientRfqService clientRfqService;
    @MockBean private ClientQuotationService clientQuotationService;

    private UserPrincipal principal(String role) {
        return new UserPrincipal(UUID.randomUUID(), role.toLowerCase() + "@tenant.com", null, UUID.randomUUID(), role, null);
    }

    @Test
    @DisplayName("viewerPodeLerRfqMasNaoCriar")
    void viewerPodeLerRfqMasNaoCriar() throws Exception {
        String validRfqBody = """
                {
                  "reference":"RFQ-001",
                  "prospectCompanyName":"Prospect Ocean",
                  "contactName":"Maria",
                  "contactEmail":"maria@test.com",
                  "direction":"EXPORT",
                  "transportMode":"OCEAN",
                  "serviceType":"LCL",
                  "originPortId":"11111111-1111-1111-1111-111111111111",
                  "destinationPortId":"22222222-2222-2222-2222-222222222222",
                  "cargoItems":[
                    {
                      "description":"Electronics",
                      "packageQuantity":1,
                      "grossWeight":100,
                      "weightUnit":"KG"
                    }
                  ]
                }
                """;

        mockMvc.perform(get("/api/v1/commercial/rfqs").with(user(principal("VIEWER"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/commercial/rfqs")
                        .with(csrf())
                        .with(user(principal("VIEWER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRfqBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("clientNaoTemAcessoAoModuloComercial")
    void clientNaoTemAcessoAoModuloComercial() throws Exception {
        mockMvc.perform(get("/api/v1/commercial/rfqs").with(user(principal("CLIENT"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/commercial/quotations").with(user(principal("CLIENT"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("clientPodeAcessarEndpointsDoPortal")
    void clientPodeAcessarEndpointsDoPortal() throws Exception {
        mockMvc.perform(get("/api/v1/client/rfqs").with(user(principal("CLIENT"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/client/quotations").with(user(principal("CLIENT"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("rolesInternasNaoGanhamAcessoAoPortalClient")
    void rolesInternasNaoGanhamAcessoAoPortalClient() throws Exception {
        mockMvc.perform(get("/api/v1/client/rfqs").with(user(principal("ADMIN"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/client/rfqs").with(user(principal("OPERATOR"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/client/rfqs").with(user(principal("VIEWER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("operatorPodeCriarCotacao")
    void operatorPodeCriarCotacao() throws Exception {
        String body = """
                {
                  "quotationNumber":"Q-001",
                  "sellingCurrency":"USD"
                }
                """;

        mockMvc.perform(post("/api/v1/commercial/rfqs/{rfqId}/quotations", UUID.randomUUID())
                        .with(csrf())
                        .with(user(principal("OPERATOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("operatorNaoPodeAprovarNemEnviarCotacao")
    void operatorNaoPodeAprovarNemEnviarCotacao() throws Exception {
        mockMvc.perform(post("/api/v1/commercial/quotations/{id}/approve", UUID.randomUUID())
                        .with(csrf())
                        .with(user(principal("OPERATOR"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/commercial/quotations/{id}/send", UUID.randomUUID())
                        .with(csrf())
                        .with(user(principal("OPERATOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("adminPodeAprovarEEnviarCotacao")
    void adminPodeAprovarEEnviarCotacao() throws Exception {
        mockMvc.perform(post("/api/v1/commercial/quotations/{id}/approve", UUID.randomUUID())
                        .with(csrf())
                        .with(user(principal("ADMIN"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/commercial/quotations/{id}/send", UUID.randomUUID())
                        .with(csrf())
                        .with(user(principal("ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("operatorNaoPodeExcluirRfq")
    void operatorNaoPodeExcluirRfq() throws Exception {
        mockMvc.perform(delete("/api/v1/commercial/rfqs/{id}", UUID.randomUUID())
                        .with(csrf())
                        .with(user(principal("OPERATOR"))))
                .andExpect(status().isForbidden());
    }
}
