package com.freightflow.modules.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.document.dto.DocumentResponse;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.exception.ResourceNotFoundException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DocumentController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, DocumentControllerTest.RoleAspectTestConfig.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("DocumentController")
class DocumentControllerTest {

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

    @MockBean private DocumentService documentService;

    private final UserPrincipal adminPrincipal = TestDataFactory.principal();
    private final UserPrincipal viewerPrincipal = new UserPrincipal(
            UUID.fromString("bbbb0000-0000-0000-0000-000000000002"),
            "viewer@mercosul.com",
            null,
            TestDataFactory.defaultTenantId(),
            "VIEWER",
            null
    );
    private final UserPrincipal clientPrincipal = new UserPrincipal(
            UUID.fromString("bbbb0000-0000-0000-0000-000000000003"),
            "client@mercosul.com",
            null,
            TestDataFactory.defaultTenantId(),
            "CLIENT",
            UUID.fromString("99990000-0000-0000-0000-000000000001")
    );

    private DocumentResponse sampleDocument() {
        return new DocumentResponse(
                UUID.randomUUID(),
                TestDataFactory.defaultShipmentId(),
                DocumentType.BL,
                "invoice.pdf",
                "application/pdf",
                123L,
                "Invoice",
                TestDataFactory.defaultUserId(),
                Instant.now(),
                "https://signed"
        );
    }

    @Nested
    @DisplayName("GET /api/v1/shipments/{id}/documents")
    class ListByShipment {

        @Test
        @DisplayName("Deve retornar 200 com lista de documentos do shipment")
        void deveRetornar200ComLista() throws Exception {
            UUID shipmentId = TestDataFactory.defaultShipmentId();
            when(documentService.listByShipment(TestDataFactory.defaultTenantId(), shipmentId, null))
                    .thenReturn(List.of(sampleDocument()));

            mockMvc.perform(get("/api/v1/shipments/{id}/documents", shipmentId)
                            .with(user(adminPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].shipmentId").value(shipmentId.toString()));
        }

        @Test
        @DisplayName("CLIENT repassa customerId ao listar documentos")
        void clientRepassaCustomerIdAoListarDocumentos() throws Exception {
            UUID shipmentId = TestDataFactory.defaultShipmentId();
            when(documentService.listByShipment(TestDataFactory.defaultTenantId(), shipmentId, clientPrincipal.getCustomerId()))
                    .thenReturn(List.of(sampleDocument()));

            mockMvc.perform(get("/api/v1/shipments/{id}/documents", shipmentId)
                            .with(user(clientPrincipal)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/shipments/{id}/documents")
    class Upload {

        @Test
        @DisplayName("ADMIN pode fazer upload")
        void adminPodeFazerUpload() throws Exception {
            UUID shipmentId = TestDataFactory.defaultShipmentId();
            MockMultipartFile file = new MockMultipartFile(
                    "file", "invoice.pdf", "application/pdf", "pdf-content".getBytes());

            when(documentService.upload(eq(TestDataFactory.defaultTenantId()), eq(shipmentId), eq(null), any(), eq("BL"), eq("Invoice"), any()))
                    .thenReturn(sampleDocument());

            mockMvc.perform(multipart("/api/v1/shipments/{id}/documents", shipmentId)
                            .file(file)
                            .param("type", "BL")
                            .param("description", "Invoice")
                            .with(csrf())
                            .with(user(adminPrincipal)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.fileName").value("invoice.pdf"));
        }

        @Test
        @DisplayName("VIEWER nao pode fazer upload")
        void viewerNaoPodeFazerUpload() throws Exception {
            UUID shipmentId = TestDataFactory.defaultShipmentId();
            MockMultipartFile file = new MockMultipartFile(
                    "file", "invoice.pdf", "application/pdf", "pdf-content".getBytes());

            mockMvc.perform(multipart("/api/v1/shipments/{id}/documents", shipmentId)
                            .file(file)
                            .param("type", "BL")
                            .with(csrf())
                            .with(user(viewerPrincipal)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CLIENT nao pode fazer upload")
        void clientNaoPodeFazerUpload() throws Exception {
            UUID shipmentId = TestDataFactory.defaultShipmentId();
            MockMultipartFile file = new MockMultipartFile(
                    "file", "invoice.pdf", "application/pdf", "pdf-content".getBytes());

            mockMvc.perform(multipart("/api/v1/shipments/{id}/documents", shipmentId)
                            .file(file)
                            .param("type", "BL")
                            .with(csrf())
                            .with(user(clientPrincipal)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/documents/{id}")
    class Delete {

        @Test
        @DisplayName("ADMIN pode remover documento")
        void adminPodeRemoverDocumento() throws Exception {
            UUID documentId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/documents/{id}", documentId)
                            .with(csrf())
                            .with(user(adminPrincipal)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 404 quando documento nao existe no escopo")
        void deveRetornar404QuandoDocumentoNaoExisteNoEscopo() throws Exception {
            UUID documentId = UUID.randomUUID();
            doThrow(new ResourceNotFoundException("Document", documentId))
                    .when(documentService).delete(TestDataFactory.defaultTenantId(), documentId, null);

            mockMvc.perform(delete("/api/v1/documents/{id}", documentId)
                            .with(csrf())
                            .with(user(adminPrincipal)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("VIEWER nao pode remover documento")
        void viewerNaoPodeRemoverDocumento() throws Exception {
            UUID documentId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/documents/{id}", documentId)
                            .with(csrf())
                            .with(user(viewerPrincipal)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CLIENT nao pode remover documento")
        void clientNaoPodeRemoverDocumento() throws Exception {
            UUID documentId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/documents/{id}", documentId)
                            .with(csrf())
                            .with(user(clientPrincipal)))
                    .andExpect(status().isForbidden());
        }
    }
}
