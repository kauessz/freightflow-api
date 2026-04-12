package com.freightflow.modules.shipment;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.modules.shipment.dto.CreateShipmentRequest;
import com.freightflow.modules.shipment.dto.ShipmentResponse;
import com.freightflow.modules.shipment.dto.ShipmentStatsResponse;
import com.freightflow.modules.shipment.dto.UpdateShipmentRequest;
import com.freightflow.modules.shipment.enums.ContainerType;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.modules.shipment.service.ShipmentService;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.VoyageRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentService")
class ShipmentServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private VoyageRepository voyageRepository;
    @Mock private PortRepository portRepository;
    @Mock private TenantRepository tenantRepository;

    @InjectMocks private ShipmentService shipmentService;

    private Shipment shipment;
    private Voyage voyage;
    private Port santos;
    private Port rotterdam;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = TestDataFactory.tenant();
        santos = TestDataFactory.santos();
        rotterdam = TestDataFactory.rotterdam();
        voyage = TestDataFactory.voyage();
        shipment = TestDataFactory.shipment();
    }

    // ==================== list ====================

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("Deve retornar pagina de shipments do tenant")
        void deveRetornarPaginaDeShipments() {
            Pageable pageable = PageRequest.of(0, 20);
            var page = new PageImpl<>(List.of(shipment), pageable, 1);
            when(shipmentRepository.findByTenantId(tenant.getId(), pageable)).thenReturn(page);

            PageResponse<ShipmentResponse> result = shipmentService.list(tenant.getId(), pageable);

            assertThat(result.data()).hasSize(1);
            assertThat(result.meta().total()).isEqualTo(1);
            assertThat(result.data().get(0).booking()).isEqualTo("A123456789");
            verify(shipmentRepository).findByTenantId(tenant.getId(), pageable);
        }

        @Test
        @DisplayName("Deve retornar pagina vazia quando nao tem shipments")
        void deveRetornarPaginaVazia() {
            Pageable pageable = PageRequest.of(0, 20);
            var page = new PageImpl<Shipment>(List.of(), pageable, 0);
            when(shipmentRepository.findByTenantId(tenant.getId(), pageable)).thenReturn(page);

            PageResponse<ShipmentResponse> result = shipmentService.list(tenant.getId(), pageable);

            assertThat(result.data()).isEmpty();
            assertThat(result.meta().total()).isEqualTo(0);
        }
    }

    // ==================== getById ====================

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Deve retornar shipment quando encontrado no tenant correto")
        void deveRetornarShipmentQuandoEncontrado() {
            UUID tenantId = tenant.getId();
            when(shipmentRepository.findByIdAndTenantId(shipment.getId(), tenantId))
                    .thenReturn(Optional.of(shipment));

            ShipmentResponse result = shipmentService.getById(shipment.getId(), tenantId);

            assertThat(result.id()).isEqualTo(shipment.getId());
            assertThat(result.booking()).isEqualTo("A123456789");
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando shipment nao encontrado ou pertence a outro tenant")
        void deveLancarExcecaoQuandoNaoEncontradoOuOutroTenant() {
            UUID id = UUID.randomUUID();
            UUID tenantId = tenant.getId();
            when(shipmentRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shipmentService.getById(id, tenantId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");
        }
    }

    // ==================== create ====================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        private CreateShipmentRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new CreateShipmentRequest(
                    "B987654321", "CMAU7654321", ContainerType.TEU40HC,
                    voyage.getId(), santos.getId(), rotterdam.getId(),
                    "European Imports BV", "Brazil Exports Ltda"
            );
        }

        @Test
        @DisplayName("Deve criar shipment com sucesso")
        void deveCriarShipmentComSucesso() {
            when(shipmentRepository.existsByBooking("B987654321")).thenReturn(false);
            when(voyageRepository.findById(voyage.getId())).thenReturn(Optional.of(voyage));
            when(portRepository.findById(santos.getId())).thenReturn(Optional.of(santos));
            when(portRepository.findById(rotterdam.getId())).thenReturn(Optional.of(rotterdam));
            when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> {
                Shipment s = inv.getArgument(0);
                return s;
            });

            ShipmentResponse result = shipmentService.create(validRequest, tenant.getId());

            assertThat(result.booking()).isEqualTo("B987654321");
            assertThat(result.status()).isEqualTo(ShipmentStatus.BOOKED);
            verify(shipmentRepository).save(any(Shipment.class));
        }

        @Test
        @DisplayName("Deve lancar BusinessException quando booking duplicado")
        void deveLancarExcecaoQuandoBookingDuplicado() {
            when(shipmentRepository.existsByBooking("B987654321")).thenReturn(true);

            assertThatThrownBy(() -> shipmentService.create(validRequest, tenant.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already exists");

            verify(shipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando voyage nao encontrado")
        void deveLancarExcecaoQuandoVoyageNaoEncontrado() {
            when(shipmentRepository.existsByBooking("B987654321")).thenReturn(false);
            when(voyageRepository.findById(voyage.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shipmentService.create(validRequest, tenant.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Voyage");
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando porto de origem nao encontrado")
        void deveLancarExcecaoQuandoPortoOrigemNaoEncontrado() {
            when(shipmentRepository.existsByBooking("B987654321")).thenReturn(false);
            when(voyageRepository.findById(voyage.getId())).thenReturn(Optional.of(voyage));
            when(portRepository.findById(santos.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shipmentService.create(validRequest, tenant.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Port");
        }
    }

    // ==================== update ====================

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Deve atualizar campos parcialmente")
        void deveAtualizarParcialmente() {
            when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateShipmentRequest request = new UpdateShipmentRequest(
                    "TRIU9999999", ContainerType.REEFER40, "New Consignee", null
            );

            ShipmentResponse result = shipmentService.update(shipment.getId(), request);

            assertThat(result.containerNumber()).isEqualTo("TRIU9999999");
            assertThat(result.containerType()).isEqualTo(ContainerType.REEFER40);
        }

        @Test
        @DisplayName("Deve manter campos nulos inalterados")
        void deveManterCamposNulosInalterados() {
            when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateShipmentRequest request = new UpdateShipmentRequest(null, null, null, null);

            ShipmentResponse result = shipmentService.update(shipment.getId(), request);

            assertThat(result.containerNumber()).isEqualTo("MSCU1234567");
            assertThat(result.containerType()).isEqualTo(ContainerType.TEU40);
        }
    }

    // ==================== delete ====================

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Deve deletar shipment existente")
        void deveDeletarShipmentExistente() {
            when(shipmentRepository.existsById(shipment.getId())).thenReturn(true);

            shipmentService.delete(shipment.getId());

            verify(shipmentRepository).deleteById(shipment.getId());
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando shipment nao existe")
        void deveLancarExcecaoQuandoNaoExiste() {
            UUID id = UUID.randomUUID();
            when(shipmentRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> shipmentService.delete(id))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(shipmentRepository, never()).deleteById(any());
        }
    }

    // ==================== getStats ====================

    @Nested
    @DisplayName("getStats()")
    class GetStatsTests {

        @Test
        @DisplayName("Deve retornar stats corretos para o tenant")
        void deveRetornarStatsCorretos() {
            UUID tenantId = tenant.getId();

            when(shipmentRepository.countByTenantId(tenantId)).thenReturn(10L);
            when(shipmentRepository.countByTenantIdAndStatus(tenantId, ShipmentStatus.IN_TRANSIT))
                    .thenReturn(4L);
            when(shipmentRepository.countByTenantIdAndStatus(tenantId, ShipmentStatus.ARRIVED))
                    .thenReturn(3L);
            when(shipmentRepository.countDelayed(eq(tenantId), eq(ShipmentStatus.IN_TRANSIT)))
                    .thenReturn(2L);
            when(shipmentRepository.countAtRisk(eq(tenantId)))
                    .thenReturn(1L);

            ShipmentStatsResponse stats = shipmentService.getStats(tenantId);

            assertThat(stats.total()).isEqualTo(10L);
            assertThat(stats.inTransit()).isEqualTo(4L);
            assertThat(stats.arrived()).isEqualTo(3L);
            assertThat(stats.delayed()).isEqualTo(2L);
            assertThat(stats.atRisk()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve retornar zeros quando nao ha embarques")
        void deveRetornarZerosQuandoSemEmbarques() {
            UUID tenantId = tenant.getId();

            when(shipmentRepository.countByTenantId(tenantId)).thenReturn(0L);
            when(shipmentRepository.countByTenantIdAndStatus(tenantId, ShipmentStatus.IN_TRANSIT))
                    .thenReturn(0L);
            when(shipmentRepository.countByTenantIdAndStatus(tenantId, ShipmentStatus.ARRIVED))
                    .thenReturn(0L);
            when(shipmentRepository.countDelayed(eq(tenantId), eq(ShipmentStatus.IN_TRANSIT)))
                    .thenReturn(0L);
            when(shipmentRepository.countAtRisk(eq(tenantId)))
                    .thenReturn(0L);

            ShipmentStatsResponse stats = shipmentService.getStats(tenantId);

            assertThat(stats.total()).isZero();
            assertThat(stats.delayed()).isZero();
            assertThat(stats.atRisk()).isZero();
        }

        @Test
        @DisplayName("Deve contar apenas IN_TRANSIT para delayed")
        void deveContarApenasInTransitParaDelayed() {
            UUID tenantId = tenant.getId();

            when(shipmentRepository.countByTenantId(tenantId)).thenReturn(5L);
            when(shipmentRepository.countByTenantIdAndStatus(any(), any())).thenReturn(0L);
            when(shipmentRepository.countDelayed(any(), any())).thenReturn(3L);
            when(shipmentRepository.countAtRisk(any())).thenReturn(0L);

            ShipmentStatsResponse stats = shipmentService.getStats(tenantId);

            verify(shipmentRepository).countDelayed(eq(tenantId), eq(ShipmentStatus.IN_TRANSIT));
            assertThat(stats.delayed()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Deve contar HIGH e CRITICAL para atRisk")
        void deveContarHighECriticalParaAtRisk() {
            UUID tenantId = tenant.getId();

            when(shipmentRepository.countByTenantId(any())).thenReturn(0L);
            when(shipmentRepository.countByTenantIdAndStatus(any(), any())).thenReturn(0L);
            when(shipmentRepository.countDelayed(any(), any())).thenReturn(0L);
            when(shipmentRepository.countAtRisk(eq(tenantId))).thenReturn(5L);

            ShipmentStatsResponse stats = shipmentService.getStats(tenantId);

            verify(shipmentRepository).countAtRisk(eq(tenantId));
            assertThat(stats.atRisk()).isEqualTo(5L);
        }
    }
}
