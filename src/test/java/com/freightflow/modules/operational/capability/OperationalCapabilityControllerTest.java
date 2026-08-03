package com.freightflow.modules.operational.capability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.security.UserPrincipal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OperationalCapabilityController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("OperationalCapabilityController")
class OperationalCapabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @SuppressWarnings("unused")
    private ObjectMapper objectMapper;

    @MockBean
    private OperationalCapabilityService operationalCapabilityService;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @DisplayName("rolesTenantPermitidasRecebem200")
    void rolesTenantPermitidasRecebem200(String role) throws Exception {
        UUID tenantId = UUID.randomUUID();
        when(operationalCapabilityService.getCapabilities(tenantId)).thenReturn(snapshot());

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .with(user(principal(role, tenantId))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.capabilities[0].key").value("CLIENT_PORTAL"))
                .andExpect(jsonPath("$.capabilities[0].available").value(true))
                .andExpect(jsonPath("$.capabilities[1].key").value("COMMERCIAL_RFQ"))
                .andExpect(jsonPath("$.capabilities[2].key").value("QUOTATION_WORKFLOW"))
                .andExpect(jsonPath("$.evaluatedAt").value("2026-08-03T12:00:00Z"));
    }

    @Test
    @DisplayName("semTokenRecebe401")
    void semTokenRecebe401() throws Exception {
        mockMvc.perform(get("/api/v1/me/capabilities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ignoraTenantIdExternoEUsaSomenteOTenantDoPrincipal")
    void ignoraTenantIdExternoEUsaSomenteOTenantDoPrincipal() throws Exception {
        UUID principalTenantId = UUID.randomUUID();
        when(operationalCapabilityService.getCapabilities(principalTenantId)).thenReturn(snapshot());

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .param("tenantId", UUID.randomUUID().toString())
                        .with(user(principal("ADMIN", principalTenantId))))
                .andExpect(status().isOk());

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(operationalCapabilityService).getCapabilities(captor.capture());
        assertThat(captor.getValue()).isEqualTo(principalTenantId);
    }

    @Test
    @DisplayName("jsonNaoExpoeDetalhesAdministrativos")
    void jsonNaoExpoeDetalhesAdministrativos() throws Exception {
        UUID tenantId = UUID.randomUUID();
        when(operationalCapabilityService.getCapabilities(tenantId)).thenReturn(snapshot());

        mockMvc.perform(get("/api/v1/me/capabilities")
                        .with(user(principal("CLIENT", tenantId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").doesNotExist())
                .andExpect(jsonPath("$.customerId").doesNotExist())
                .andExpect(jsonPath("$.plan").doesNotExist())
                .andExpect(jsonPath("$.subscription").doesNotExist())
                .andExpect(jsonPath("$.accessStatus").doesNotExist())
                .andExpect(jsonPath("$.enforcementMode").doesNotExist())
                .andExpect(jsonPath("$.entitled").doesNotExist())
                .andExpect(jsonPath("$.effectiveEnabled").doesNotExist())
                .andExpect(jsonPath("$.allowedByRollout").doesNotExist())
                .andExpect(jsonPath("$.denialReason").doesNotExist())
                .andExpect(jsonPath("$.warnings").doesNotExist())
                .andExpect(jsonPath("$.dependencies").doesNotExist())
                .andExpect(jsonPath("$.limits").doesNotExist());
    }

    private OperationalCapabilitySnapshot snapshot() {
        return new OperationalCapabilitySnapshot(
                List.of(
                        new OperationalCapabilityAvailability("CLIENT_PORTAL", true),
                        new OperationalCapabilityAvailability("COMMERCIAL_RFQ", false),
                        new OperationalCapabilityAvailability("QUOTATION_WORKFLOW", true)
                ),
                Instant.parse("2026-08-03T12:00:00Z")
        );
    }

    private UserPrincipal principal(String role, UUID tenantId) {
        UUID customerId = "CLIENT".equals(role) ? UUID.randomUUID() : null;
        return new UserPrincipal(
                UUID.randomUUID(),
                role.toLowerCase() + "@tenant.test",
                null,
                tenantId,
                role,
                customerId
        );
    }
}
