package com.freightflow.modules.document;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.document.dto.DocumentResponse;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService")
class DocumentServiceTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private UserRepository userRepository;
    @Mock private StorageService storageService;

    @InjectMocks private DocumentService documentService;

    private Tenant tenant;
    private Shipment shipment;
    private UUID tenantId;
    private UUID shipmentId;

    @BeforeEach
    void setUp() {
        tenant = TestDataFactory.tenant();
        shipment = TestDataFactory.shipment();
        tenantId = tenant.getId();
        shipmentId = shipment.getId();
    }

    @Nested
    @DisplayName("upload()")
    class UploadTests {

        @Test
        @DisplayName("should_uploadDocument_when_authorized")
        void should_uploadDocument_when_authorized() {
            MockMultipartFile file = pdfFile();
            Document saved = document(shipment);

            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.of(shipment));
            when(storageService.upload(anyString(), anyString(), anyString(), anyString(), any(byte[].class)))
                    .thenReturn("tenant/shipment/file.pdf");
            when(documentRepository.save(any(Document.class))).thenReturn(saved);
            when(storageService.generatePresignedUrl(anyString(), any(Duration.class))).thenReturn("https://signed");

            DocumentResponse result = documentService.upload(
                    tenantId, shipmentId, null, null, "BL", "Invoice", file);

            assertThat(result.id()).isEqualTo(saved.getId());
            verify(storageService).upload(anyString(), anyString(), anyString(), anyString(), any(byte[].class));
        }

        @Test
        @DisplayName("should_notUpload_when_shipmentBelongsToAnotherTenant")
        void should_notUpload_when_shipmentBelongsToAnotherTenant() {
            MockMultipartFile file = pdfFile();

            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.upload(
                    tenantId, shipmentId, null, null, "BL", "Invoice", file))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");

            verify(storageService, never()).upload(anyString(), anyString(), anyString(), anyString(), any(byte[].class));
            verify(documentRepository, never()).save(any(Document.class));
        }

        @Test
        @DisplayName("should_notUpload_when_clientTargetsAnotherCustomer")
        void should_notUpload_when_clientTargetsAnotherCustomer() {
            MockMultipartFile file = pdfFile();
            UUID customerId = UUID.randomUUID();

            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(shipmentRepository.findByIdAndTenantIdAndCustomerId(shipmentId, tenantId, customerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.upload(
                    tenantId, shipmentId, customerId, null, "BL", "Invoice", file))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");

            verify(storageService, never()).upload(anyString(), anyString(), anyString(), anyString(), any(byte[].class));
            verify(documentRepository, never()).save(any(Document.class));
        }
    }

    @Nested
    @DisplayName("listByShipment()")
    class ListTests {

        @Test
        @DisplayName("should_listDocuments_when_authorized")
        void should_listDocuments_when_authorized() {
            Document doc = document(shipment);

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.of(shipment));
            when(documentRepository.findByShipmentIdAndShipmentTenantIdAndActiveTrue(shipmentId, tenantId))
                    .thenReturn(List.of(doc));
            when(storageService.generatePresignedUrl(anyString(), any(Duration.class))).thenReturn("https://signed");

            List<DocumentResponse> result = documentService.listByShipment(tenantId, shipmentId, null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should_notListDocuments_when_shipmentBelongsToAnotherTenant")
        void should_notListDocuments_when_shipmentBelongsToAnotherTenant() {
            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.listByShipment(tenantId, shipmentId, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");

            verify(storageService, never()).generatePresignedUrl(anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("should_notListDocuments_when_clientTargetsAnotherCustomer")
        void should_notListDocuments_when_clientTargetsAnotherCustomer() {
            UUID customerId = UUID.randomUUID();
            when(shipmentRepository.findByIdAndTenantIdAndCustomerId(shipmentId, tenantId, customerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.listByShipment(tenantId, shipmentId, customerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");

            verify(storageService, never()).generatePresignedUrl(anyString(), any(Duration.class));
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should_deleteDocument_when_authorized")
        void should_deleteDocument_when_authorized() {
            Document doc = document(shipment);

            when(documentRepository.findByIdAndShipmentTenantIdAndActiveTrue(doc.getId(), tenantId))
                    .thenReturn(Optional.of(doc));
            when(documentRepository.save(any(Document.class))).thenReturn(doc);

            documentService.delete(tenantId, doc.getId(), null);

            verify(storageService).delete(doc.getStorageKey());
        }

        @Test
        @DisplayName("should_notDeleteDocument_when_documentBelongsToAnotherTenant")
        void should_notDeleteDocument_when_documentBelongsToAnotherTenant() {
            UUID documentId = UUID.randomUUID();
            when(documentRepository.findByIdAndShipmentTenantIdAndActiveTrue(documentId, tenantId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.delete(tenantId, documentId, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Document");

            verify(storageService, never()).delete(anyString());
        }

        @Test
        @DisplayName("should_notDeleteDocument_when_clientTargetsAnotherCustomer")
        void should_notDeleteDocument_when_clientTargetsAnotherCustomer() {
            UUID documentId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            when(documentRepository.findByIdAndShipmentTenantIdAndShipmentCustomerIdAndActiveTrue(
                    documentId, tenantId, customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.delete(tenantId, documentId, customerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Document");

            verify(storageService, never()).delete(anyString());
        }
    }

    private MockMultipartFile pdfFile() {
        return new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );
    }

    private Document document(Shipment shipment) {
        Document doc = new Document();
        doc.setTenant(tenant);
        doc.setShipment(shipment);
        doc.setType(DocumentType.BL);
        doc.setFileName("invoice.pdf");
        doc.setStorageKey("tenant/shipment/file.pdf");
        doc.setContentType("application/pdf");
        doc.setSizeBytes(123L);
        doc.setDescription("Invoice");
        doc.setUploadedAt(Instant.now());
        doc.setActive(true);
        TestDataFactory.setEntityId(doc, UUID.randomUUID());
        return doc;
    }
}
