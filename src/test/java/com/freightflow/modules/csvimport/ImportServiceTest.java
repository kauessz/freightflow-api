package com.freightflow.modules.csvimport;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.csvimport.dto.ImportResult;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.VoyageRepository;
import com.freightflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImportService")
class ImportServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private VoyageRepository voyageRepository;
    @Mock private PortRepository portRepository;
    @Mock private TenantRepository tenantRepository;

    @InjectMocks private ImportService importService;

    private Tenant tenant;
    private Voyage voyage;
    private Port santos;
    private Port rotterdam;

    @BeforeEach
    void setUp() {
        tenant = TestDataFactory.tenant();
        voyage = TestDataFactory.voyage();
        santos = TestDataFactory.santos();
        rotterdam = TestDataFactory.rotterdam();
    }

    private MockMultipartFile csvFile(String content) {
        return new MockMultipartFile(
                "file", "shipments.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    // ==================== Importacao com sucesso ====================

    @Nested
    @DisplayName("Importacao com sucesso")
    class ImportacaoSucesso {

        @Test
        @DisplayName("Deve importar CSV valido com 2 linhas")
        void deveImportarCsvValido() {
            String csv = TestDataFactory.validCsvContent();
            MockMultipartFile file = csvFile(csv);

            when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(shipmentRepository.existsByBookingAndTenantId(any(), eq(tenant.getId()))).thenReturn(false);
            when(shipmentRepository.existsByBooking(any())).thenReturn(false);
            when(voyageRepository.findByVoyageNumber("MSC-2026-001")).thenReturn(Optional.of(voyage));
            when(portRepository.findByUnlocode("BRSSZ")).thenReturn(Optional.of(santos));
            when(portRepository.findByUnlocode("NLRTM")).thenReturn(Optional.of(rotterdam));
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

            ImportResult result = importService.importShipments(file, tenant.getId());

            assertThat(result.totalRows()).isEqualTo(2);
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.errorCount()).isEqualTo(0);
            assertThat(result.imported()).hasSize(2);
            assertThat(result.imported().get(0).booking()).isEqualTo("A111111111");
            assertThat(result.imported().get(1).booking()).isEqualTo("B222222222");
            verify(shipmentRepository, times(2)).save(any(Shipment.class));
        }
    }

    // ==================== Validacao de arquivo ====================

    @Nested
    @DisplayName("Validacao de arquivo")
    class ValidacaoArquivo {

        @Test
        @DisplayName("Deve rejeitar arquivo vazio")
        void deveRejeitarArquivoVazio() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "empty.csv", "text/csv", new byte[0]);

            assertThatThrownBy(() -> importService.importShipments(file, tenant.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("Deve rejeitar arquivo maior que 5MB")
        void deveRejeitarArquivoGrande() {
            byte[] bigContent = new byte[6 * 1024 * 1024]; // 6MB
            MockMultipartFile file = new MockMultipartFile(
                    "file", "big.csv", "text/csv", bigContent);

            // validateFile() lanca excecao ANTES de tenantRepository.findById()
            assertThatThrownBy(() -> importService.importShipments(file, tenant.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("5MB");
        }

        @Test
        @DisplayName("Deve rejeitar arquivo sem extensao .csv")
        void deveRejeitarExtensaoInvalida() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "data.xlsx", "application/octet-stream",
                    "some content".getBytes());

            // validateFile() lanca excecao ANTES de tenantRepository.findById()
            assertThatThrownBy(() -> importService.importShipments(file, tenant.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(".csv");
        }

        @Test
        @DisplayName("Deve rejeitar CSV com header invalido")
        void deveRejeitarHeaderInvalido() {
            MockMultipartFile file = csvFile(TestDataFactory.csvWithInvalidHeader());

            when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

            assertThatThrownBy(() -> importService.importShipments(file, tenant.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid CSV header");
        }

        @Test
        @DisplayName("Deve rejeitar CSV apenas com header (sem dados)")
        void deveRejeitarCsvSemDados() {
            String csv = "booking,containerNumber,containerType,voyageNumber,originUnlocode,destinationUnlocode,shipper,consignee\n";
            MockMultipartFile file = csvFile(csv);

            when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

            assertThatThrownBy(() -> importService.importShipments(file, tenant.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no data");
        }
    }

    // ==================== Validacao de linhas ====================

    @Nested
    @DisplayName("Validacao de linhas")
    class ValidacaoLinhas {

        @Test
        @DisplayName("Deve reportar erros de validacao sintatica por linha")
        void deveReportarErrosSintaticos() {
            // csvWithErrors tem 3 linhas:
            // Row 2: "INVALID,,TEU40,MSC-2026-001,BRSSZ,NLRTM,..." — booking invalido → isValid()=false
            // Row 3: "A333333333,...,INVALID_TYPE,MSC-2026-001,..." — containerType invalido → isValid()=false
            // Row 4: "A444444444,...,TEU40,FAKE-999,BRSSZ,NLRTM,..." — valido sintaticamente → processRow
            String csv = TestDataFactory.csvWithErrors();
            MockMultipartFile file = csvFile(csv);

            when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            // Apenas row 4 chega ao processRow e precisa desses mocks:
            when(shipmentRepository.existsByBookingAndTenantId(any(), eq(tenant.getId()))).thenReturn(false);
            when(shipmentRepository.existsByBooking(any())).thenReturn(false);
            when(voyageRepository.findByVoyageNumber("FAKE-999")).thenReturn(Optional.empty());
            when(portRepository.findByUnlocode("BRSSZ")).thenReturn(Optional.of(santos));
            when(portRepository.findByUnlocode("NLRTM")).thenReturn(Optional.of(rotterdam));

            ImportResult result = importService.importShipments(file, tenant.getId());

            // Row 2: INVALID booking pattern
            // Row 3: INVALID_TYPE container type
            // Row 4: FAKE-999 voyage not found
            assertThat(result.errorCount()).isGreaterThanOrEqualTo(2);
            assertThat(result.errors()).anyMatch(e -> e.row() == 2); // INVALID booking
            assertThat(result.errors()).anyMatch(e -> e.row() == 3); // INVALID_TYPE
        }

        @Test
        @DisplayName("Deve reportar erro quando booking ja existe no tenant")
        void deveReportarBookingDuplicado() {
            String csv = "booking,containerNumber,containerType,voyageNumber,originUnlocode,destinationUnlocode,shipper,consignee\n"
                    + "A111111111,MSCU1111111,TEU40,MSC-2026-001,BRSSZ,NLRTM,Shipper,Consignee\n";
            MockMultipartFile file = csvFile(csv);

            when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(shipmentRepository.existsByBookingAndTenantId("A111111111", tenant.getId())).thenReturn(true);
            when(shipmentRepository.existsByBooking("A111111111")).thenReturn(true);

            ImportResult result = importService.importShipments(file, tenant.getId());

            assertThat(result.errorCount()).isEqualTo(1);
            assertThat(result.errors().get(0).errors()).anyMatch(e -> e.contains("already exists"));
        }

        @Test
        @DisplayName("Deve reportar erro quando voyage nao encontrado")
        void deveReportarVoyageNaoEncontrado() {
            String csv = "booking,containerNumber,containerType,voyageNumber,originUnlocode,destinationUnlocode,shipper,consignee\n"
                    + "A555555555,MSCU5555555,TEU40,UNKNOWN-001,BRSSZ,NLRTM,Shipper,Consignee\n";
            MockMultipartFile file = csvFile(csv);

            when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(shipmentRepository.existsByBookingAndTenantId(any(), eq(tenant.getId()))).thenReturn(false);
            when(shipmentRepository.existsByBooking(any())).thenReturn(false);
            when(voyageRepository.findByVoyageNumber("UNKNOWN-001")).thenReturn(Optional.empty());
            when(portRepository.findByUnlocode("BRSSZ")).thenReturn(Optional.of(santos));
            when(portRepository.findByUnlocode("NLRTM")).thenReturn(Optional.of(rotterdam));

            ImportResult result = importService.importShipments(file, tenant.getId());

            assertThat(result.errorCount()).isEqualTo(1);
            assertThat(result.errors().get(0).errors()).anyMatch(e -> e.contains("Voyage not found"));
        }
    }

    // ==================== Template ====================

    @Nested
    @DisplayName("Template e documentacao")
    class TemplateTests {

        @Test
        @DisplayName("Deve gerar template CSV com header e exemplos")
        void deveGerarTemplate() {
            String template = importService.generateTemplate();

            assertThat(template).startsWith("booking,containerNumber,containerType");
            assertThat(template).contains("A123456789");
            assertThat(template).contains("MSCU1234567");
            assertThat(template.split("\n").length).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Deve retornar documentacao do formato em JSON")
        void deveRetornarDocumentacao() {
            String docs = importService.getFormatDocumentation();

            assertThat(docs).contains("CSV");
            assertThat(docs).contains("500");
            assertThat(docs).contains("5MB");
            assertThat(docs).contains("booking");
        }
    }
}
