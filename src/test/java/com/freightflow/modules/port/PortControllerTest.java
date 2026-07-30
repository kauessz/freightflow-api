package com.freightflow.modules.port;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.port.dto.CreatePortRequest;
import com.freightflow.modules.port.dto.PortResponse;
import com.freightflow.modules.port.dto.UpdatePortRequest;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.pagination.PageResponse;
import com.freightflow.shared.rbac.RoleCheckAspect;
import com.freightflow.shared.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PortController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, PortControllerTest.RoleAspectTestConfig.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("PortController")
class PortControllerTest {

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

    @MockBean private PortService portService;

    private final UserPrincipal adminPrincipal = TestDataFactory.principal();
    private final UserPrincipal operatorPrincipal = new UserPrincipal(
            UUID.fromString("bbbb0000-0000-0000-0000-000000000010"),
            "operator@mercosul.com",
            null,
            TestDataFactory.defaultTenantId(),
            "OPERATOR",
            null
    );
    private final UserPrincipal viewerPrincipal = new UserPrincipal(
            UUID.fromString("bbbb0000-0000-0000-0000-000000000011"),
            "viewer@mercosul.com",
            null,
            TestDataFactory.defaultTenantId(),
            "VIEWER",
            null
    );
    private final UserPrincipal clientPrincipal = new UserPrincipal(
            UUID.fromString("bbbb0000-0000-0000-0000-000000000012"),
            "client@mercosul.com",
            null,
            TestDataFactory.defaultTenantId(),
            "CLIENT",
            UUID.fromString("99990000-0000-0000-0000-000000000001")
    );

    private PortResponse santosResponse() {
        return PortResponse.from(TestDataFactory.santos());
    }

    @Nested
    @DisplayName("GET reads")
    class ReadAccess {

        @Test
        @DisplayName("CLIENT pode listar portos")
        void clientCanListPorts() throws Exception {
            when(portService.list(any())).thenReturn(new PageResponse<>(
                    List.of(santosResponse()),
                    new PageResponse.Meta(1, 0, 5, 1)
            ));

            mockMvc.perform(get("/api/v1/ports?page=0&size=5")
                            .with(user(clientPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].name").value("Santos"))
                    .andExpect(jsonPath("$.data[0].tenantId").doesNotExist())
                    .andExpect(jsonPath("$.data[0].customerId").doesNotExist());
        }

        @Test
        @DisplayName("CLIENT pode buscar portos")
        void clientCanSearchPorts() throws Exception {
            when(portService.search("San")).thenReturn(List.of(santosResponse()));

            mockMvc.perform(get("/api/v1/ports/search?q=San")
                            .with(user(clientPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].unlocode").value("BRSSZ"))
                    .andExpect(jsonPath("$[0].tenantId").doesNotExist())
                    .andExpect(jsonPath("$[0].customerId").doesNotExist());
        }

        @Test
        @DisplayName("CLIENT pode consultar porto por ID")
        void clientCanGetPortById() throws Exception {
            UUID portId = TestDataFactory.defaultPortOriginId();
            when(portService.getById(portId)).thenReturn(santosResponse());

            mockMvc.perform(get("/api/v1/ports/{id}", portId)
                            .with(user(clientPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(portId.toString()))
                    .andExpect(jsonPath("$.name").value("Santos"));
        }

        @Test
        @DisplayName("VIEWER continua podendo ler")
        void viewerStillCanRead() throws Exception {
            when(portService.getByUnlocode("BRSSZ")).thenReturn(santosResponse());

            mockMvc.perform(get("/api/v1/ports/unlocode/BRSSZ")
                            .with(user(viewerPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unlocode").value("BRSSZ"));
        }

        @Test
        @DisplayName("Sem autenticacao continua 401")
        void unauthenticatedReadIsStillUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/ports"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Write access")
    class WriteAccess {

        @Test
        @DisplayName("CLIENT nao pode criar porto")
        void clientCannotCreatePort() throws Exception {
            CreatePortRequest request = new CreatePortRequest("BRSSZ", "Santos", "BR", "America/Sao_Paulo", -23.9536, -46.3336, true);
            clearInvocations(portService);

            mockMvc.perform(post("/api/v1/ports")
                            .with(csrf())
                            .with(user(clientPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(portService, never()).create(any());
        }

        @Test
        @DisplayName("CLIENT nao pode atualizar porto")
        void clientCannotUpdatePort() throws Exception {
            UUID portId = TestDataFactory.defaultPortOriginId();
            UpdatePortRequest request = new UpdatePortRequest("BRSSZ", "Santos Updated", "BR", "America/Sao_Paulo", -23.9, -46.3, true);
            clearInvocations(portService);

            mockMvc.perform(put("/api/v1/ports/{id}", portId)
                            .with(csrf())
                            .with(user(clientPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(portService, never()).update(any(), any());
        }

        @Test
        @DisplayName("VIEWER continua sem poder criar")
        void viewerStillCannotCreate() throws Exception {
            CreatePortRequest request = new CreatePortRequest("BRSSZ", "Santos", "BR", "America/Sao_Paulo", -23.9536, -46.3336, true);

            mockMvc.perform(post("/api/v1/ports")
                            .with(csrf())
                            .with(user(viewerPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN e OPERATOR mantem mutacoes")
        void adminAndOperatorKeepMutations() throws Exception {
            CreatePortRequest createRequest = new CreatePortRequest("BRRIO", "Rio", "BR", "America/Sao_Paulo", -22.9, -43.2, true);
            UpdatePortRequest updateRequest = new UpdatePortRequest("BRSSZ", "Santos Updated", "BR", "America/Sao_Paulo", -23.9, -46.3, true);
            UUID portId = TestDataFactory.defaultPortOriginId();
            PortResponse rio = new PortResponse(portId, "BRRIO", "Rio", "BR", "America/Sao_Paulo", -22.9, -43.2, true);
            PortResponse updated = new PortResponse(portId, "BRSSZ", "Santos Updated", "BR", "America/Sao_Paulo", -23.9, -46.3, true);

            when(portService.create(any())).thenReturn(rio);
            when(portService.update(eq(portId), any())).thenReturn(updated);

            mockMvc.perform(post("/api/v1/ports")
                            .with(csrf())
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Rio"));

            mockMvc.perform(put("/api/v1/ports/{id}", portId)
                            .with(csrf())
                            .with(user(operatorPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Santos Updated"));
        }
    }
}
