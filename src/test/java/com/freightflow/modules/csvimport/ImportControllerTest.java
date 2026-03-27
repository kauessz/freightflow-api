package com.freightflow.modules.csvimport;

import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.csvimport.dto.ImportResult;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ImportController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("ImportController")
class ImportControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ImportService importService;

    private final UserPrincipal principal = TestDataFactory.principal();

    // ==================== POST /api/v1/import/shipments ====================

    @Nested
    @DisplayName("POST /api/v1/import/shipments")
    class ImportEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com resultado de importacao")
        void deveRetornar200ComResultado() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "shipments.csv", "text/csv",
                    TestDataFactory.validCsvContent().getBytes(StandardCharsets.UTF_8)
            );

            ImportResult result = new ImportResult(
                    2, 2, 0,
                    List.of(
                            new ImportResult.ImportedShipment(2, UUID.randomUUID(), "A111111111",
                                    "MSCU1111111", "MSC-2026-001", "BRSSZ", "NLRTM", "BOOKED"),
                            new ImportResult.ImportedShipment(3, UUID.randomUUID(), "B222222222",
                                    "CMAU2222222", "MSC-2026-001", "BRSSZ", "NLRTM", "BOOKED")
                    ),
                    List.of(),
                    Instant.now()
            );

            when(importService.importShipments(any(), eq(principal.getTenantId()))).thenReturn(result);

            mockMvc.perform(multipart("/api/v1/import/shipments")
                            .file(file)
                            .with(csrf())
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRows").value(2))
                    .andExpect(jsonPath("$.successCount").value(2))
                    .andExpect(jsonPath("$.errorCount").value(0))
                    .andExpect(jsonPath("$.imported").isArray())
                    .andExpect(jsonPath("$.imported[0].booking").value("A111111111"));
        }

        @Test
        @DisplayName("Deve retornar 200 com erros parciais")
        void deveRetornar200ComErrosParciais() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "mixed.csv", "text/csv",
                    "header\ndata".getBytes(StandardCharsets.UTF_8)
            );

            ImportResult result = new ImportResult(
                    3, 1, 2,
                    List.of(new ImportResult.ImportedShipment(2, UUID.randomUUID(), "A111111111",
                            "MSCU1111111", "MSC-2026-001", "BRSSZ", "NLRTM", "BOOKED")),
                    List.of(
                            new ImportResult.ImportError(3, "INVALID", List.of("booking pattern invalid")),
                            new ImportResult.ImportError(4, "B222222222", List.of("Voyage not found"))
                    ),
                    Instant.now()
            );

            when(importService.importShipments(any(), any())).thenReturn(result);

            mockMvc.perform(multipart("/api/v1/import/shipments")
                            .file(file)
                            .with(csrf())
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCount").value(1))
                    .andExpect(jsonPath("$.errorCount").value(2))
                    .andExpect(jsonPath("$.errors[0].row").value(3))
                    .andExpect(jsonPath("$.errors[1].errors[0]").value("Voyage not found"));
        }

        @Test
        @DisplayName("Deve retornar 409 quando arquivo excede limite de linhas")
        void deveRetornar409QuandoExcedeLimite() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "big.csv", "text/csv", "data".getBytes());

            when(importService.importShipments(any(), any()))
                    .thenThrow(new BusinessException("CSV file exceeds maximum of 500 rows"));

            mockMvc.perform(multipart("/api/v1/import/shipments")
                            .file(file)
                            .with(csrf())
                            .with(user(principal)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("CSV file exceeds maximum of 500 rows"));
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticacao")
        void deveRetornar401SemAuth() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", "data".getBytes());

            mockMvc.perform(multipart("/api/v1/import/shipments")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /api/v1/import/template ====================

    @Nested
    @DisplayName("GET /api/v1/import/template")
    class TemplateEndpoint {

        @Test
        @DisplayName("Deve retornar CSV template com Content-Disposition")
        void deveRetornarTemplate() throws Exception {
            String template = "booking,containerNumber,...\nA123456789,...\n";
            when(importService.generateTemplate()).thenReturn(template);

            mockMvc.perform(get("/api/v1/import/template")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=freightflow-shipments-template.csv"))
                    .andExpect(content().string(template));
        }
    }

    // ==================== GET /api/v1/import/formats ====================

    @Nested
    @DisplayName("GET /api/v1/import/formats")
    class FormatsEndpoint {

        @Test
        @DisplayName("Deve retornar documentacao JSON do formato CSV")
        void deveRetornarFormatos() throws Exception {
            String docs = "{\"format\":\"CSV\"}";
            when(importService.getFormatDocumentation()).thenReturn(docs);

            mockMvc.perform(get("/api/v1/import/formats")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"));
        }
    }
}
