package com.freightflow.modules.commercial.client.rfq;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqCreateRequest;
import com.freightflow.modules.commercial.client.rfq.dto.ClientRfqUpdateRequest;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.RfqCargoItem;
import com.freightflow.modules.commercial.rfq.RfqContainerRequirement;
import com.freightflow.modules.commercial.rfq.RfqRepository;
import com.freightflow.modules.commercial.rfq.RfqService;
import com.freightflow.modules.commercial.rfq.dto.CreateRfqRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqCargoItemRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqContainerRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqResponse;
import com.freightflow.modules.commercial.rfq.dto.UpdateRfqRequest;
import com.freightflow.modules.commercial.rfq.enums.RfqContainerType;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.shared.IncotermCode;
import com.freightflow.modules.commercial.shared.VolumeUnit;
import com.freightflow.modules.commercial.shared.WeightUnit;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.port.Port;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ForbiddenException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientRfqService")
class ClientRfqServiceTest {

    @Mock private RfqService rfqService;
    @Mock private RfqRepository rfqRepository;
    @Mock private QuotationRepository quotationRepository;

    @InjectMocks private ClientRfqService clientRfqService;

    private UUID tenantId;
    private UUID customerId;
    private UUID userId;
    private UUID rfqId;
    private RequestForQuotation rfq;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        userId = UUID.randomUUID();
        rfqId = UUID.randomUUID();

        Tenant tenant = new Tenant("Tenant", "tenant", "ops@test.com", "FREE");
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        Customer customer = new Customer(tenant, "Atlas Cargo");
        ReflectionTestUtils.setField(customer, "id", customerId);
        User clientUser = new User("Client User", "client@test.com", "hash", User.UserRole.CLIENT, tenant);
        ReflectionTestUtils.setField(clientUser, "id", userId);
        clientUser.setCustomer(customer);

        Port origin = new Port("BRSSZ", "Santos", "BR", "America/Sao_Paulo", null, null);
        Port destination = new Port("NLRTM", "Rotterdam", "NL", "Europe/Amsterdam", null, null);
        ReflectionTestUtils.setField(origin, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(destination, "id", UUID.randomUUID());

        rfq = new RequestForQuotation(
                tenant, "RFQ-CLI-1", "Maria", RfqDirection.EXPORT, RfqTransportMode.OCEAN,
                RfqServiceType.FCL, origin, destination, clientUser
        );
        ReflectionTestUtils.setField(rfq, "id", rfqId);
        rfq.setCustomer(customer);
        rfq.setContactEmail("maria@atlas.com");
        rfq.setIncotermCode(IncotermCode.FOB);
        rfq.setIncotermVersion("2020");
        rfq.setIncotermNamedPlace("Santos");
        rfq.setNotes("Client note");
        rfq.replaceCargoItems(List.of(validCargoEntity()));
        rfq.replaceContainerRequirements(List.of(validContainerEntity()));
    }

    @Test
    @DisplayName("deveCriarRfqVinculadaAoProprioCustomer")
    void deveCriarRfqVinculadaAoProprioCustomer() {
        ClientRfqCreateRequest request = validCreateRequest();
        when(rfqService.create(any(CreateRfqRequest.class), eq(tenantId), eq(userId)))
                .thenReturn(rfqResponse(rfqId));
        when(rfqRepository.findByIdAndTenantIdAndCustomerId(rfqId, tenantId, customerId))
                .thenReturn(Optional.of(rfq));
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        var response = clientRfqService.create(request, tenantId, customerId, userId);

        ArgumentCaptor<CreateRfqRequest> captor = ArgumentCaptor.forClass(CreateRfqRequest.class);
        verify(rfqService).create(captor.capture(), eq(tenantId), eq(userId));
        assertThat(captor.getValue().customerId()).isEqualTo(customerId);
        assertThat(captor.getValue().prospectCompanyName()).isNull();
        assertThat(response.id()).isEqualTo(rfqId.toString());
    }

    @Test
    @DisplayName("dtoDeClienteNaoAceitaTenantNemCustomerExternos")
    void dtoDeClienteNaoAceitaTenantNemCustomerExternos() {
        var createFields = java.util.Arrays.stream(ClientRfqCreateRequest.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .toList();
        var updateFields = java.util.Arrays.stream(ClientRfqUpdateRequest.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .toList();

        assertThat(createFields).doesNotContain("tenantId", "customerId", "prospectCompanyName", "assignedTo", "status");
        assertThat(updateFields).doesNotContain("tenantId", "customerId", "prospectCompanyName", "assignedTo", "status");
    }

    @Test
    @DisplayName("deveRejeitarClientSemCustomer")
    void deveRejeitarClientSemCustomer() {
        assertThatThrownBy(() -> clientRfqService.list(tenantId, null, PageRequest.of(0, 20)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("deveEditarRascunhoDoCliente")
    void deveEditarRascunhoDoCliente() {
        rfq.setStatus(RfqStatus.DRAFT);
        when(rfqRepository.findByIdAndTenantIdAndCustomerId(rfqId, tenantId, customerId))
                .thenReturn(Optional.of(rfq));
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        clientRfqService.update(rfqId, new ClientRfqUpdateRequest(
                "RFQ-CLI-2", null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, "Updated", null, null
        ), tenantId, customerId);

        verify(rfqService).update(eq(rfqId), any(UpdateRfqRequest.class), eq(tenantId));
    }

    @Test
    @DisplayName("naoDeveEditarRfqSubmetida")
    void naoDeveEditarRfqSubmetida() {
        rfq.setStatus(RfqStatus.SUBMITTED);
        when(rfqRepository.findByIdAndTenantIdAndCustomerId(rfqId, tenantId, customerId))
                .thenReturn(Optional.of(rfq));

        assertThatThrownBy(() -> clientRfqService.update(rfqId, new ClientRfqUpdateRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
        ), tenantId, customerId)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");

        verify(rfqService, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("deveCancelarRfqDraft")
    void deveCancelarRfqDraft() {
        rfq.setStatus(RfqStatus.DRAFT);
        when(rfqRepository.findByIdAndTenantIdAndCustomerId(rfqId, tenantId, customerId))
                .thenReturn(Optional.of(rfq));
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        clientRfqService.cancel(rfqId, tenantId, customerId);

        verify(rfqService).cancel(rfqId, tenantId);
    }

    @Test
    @DisplayName("deveCancelarRfqSubmitted")
    void deveCancelarRfqSubmitted() {
        rfq.setStatus(RfqStatus.SUBMITTED);
        when(rfqRepository.findByIdAndTenantIdAndCustomerId(rfqId, tenantId, customerId))
                .thenReturn(Optional.of(rfq));
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        clientRfqService.cancel(rfqId, tenantId, customerId);

        verify(rfqService).cancel(rfqId, tenantId);
    }

    @Test
    @DisplayName("naoDeveCancelarRfqUnderAnalysis")
    void naoDeveCancelarRfqUnderAnalysis() {
        rfq.setStatus(RfqStatus.UNDER_ANALYSIS);
        when(rfqRepository.findByIdAndTenantIdAndCustomerId(rfqId, tenantId, customerId))
                .thenReturn(Optional.of(rfq));

        assertThatThrownBy(() -> clientRfqService.cancel(rfqId, tenantId, customerId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT or SUBMITTED");
    }

    @Test
    @DisplayName("naoDeveAcessarRfqDeOutroCustomer")
    void naoDeveAcessarRfqDeOutroCustomer() {
        when(rfqRepository.findByIdAndTenantIdAndCustomerId(rfqId, tenantId, customerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientRfqService.getById(rfqId, tenantId, customerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("naoDeveAcessarRfqDeOutroTenant")
    void naoDeveAcessarRfqDeOutroTenant() {
        when(rfqRepository.findByIdAndTenantIdAndCustomerId(rfqId, tenantId, customerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientRfqService.submit(rfqId, tenantId, customerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deveListarRfqDoProprioCustomer")
    void deveListarRfqDoProprioCustomer() {
        when(rfqRepository.findByTenantIdAndCustomerId(tenantId, customerId, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(rfq), PageRequest.of(0, 20), 1));
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        var response = clientRfqService.list(tenantId, customerId, PageRequest.of(0, 20));

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(rfqId.toString());
    }

    @Test
    @DisplayName("deveRejeitarModalNaoOceanico")
    void deveRejeitarModalNaoOceanico() {
        ClientRfqCreateRequest request = new ClientRfqCreateRequest(
                "RFQ-CLI-1", "Maria", "maria@atlas.com", null,
                RfqDirection.EXPORT, RfqTransportMode.AIR, RfqServiceType.FCL, null, null, null,
                UUID.randomUUID(), UUID.randomUUID(), null, null, null, null, null,
                List.of(validCargoRequest()), List.of(validContainerRequest())
        );

        assertThatThrownBy(() -> clientRfqService.create(request, tenantId, customerId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("only OCEAN");
    }

    private ClientRfqCreateRequest validCreateRequest() {
        return new ClientRfqCreateRequest(
                "RFQ-CLI-1", "Maria", "maria@atlas.com", null,
                RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.FCL,
                IncotermCode.FOB, "2020", "Santos",
                UUID.randomUUID(), UUID.randomUUID(), null, null,
                Instant.parse("2026-07-21T10:00:00Z"),
                Instant.parse("2026-07-22T10:00:00Z"),
                "Needs response",
                List.of(validCargoRequest()),
                List.of(validContainerRequest())
        );
    }

    private RfqResponse rfqResponse(UUID id) {
        return new RfqResponse(
                id.toString(), "RFQ-CLI-1", customerId.toString(), "Atlas Cargo", null, "Maria",
                "maria@atlas.com", null, RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.FCL,
                IncotermCode.FOB, "2020", "Santos", "FOB 2020 - Santos",
                UUID.randomUUID().toString(), "Santos", "BRSSZ",
                UUID.randomUUID().toString(), "Rotterdam", "NLRTM",
                null, null, null, null, RfqStatus.DRAFT,
                null, null, "Client note", userId.toString(), "Client User",
                null, null, Instant.now(), Instant.now(), 0L, List.of(), List.of()
        );
    }

    private RfqCargoItemRequest validCargoRequest() {
        return new RfqCargoItemRequest(
                "Electronics", "PALLET", 10, BigDecimal.valueOf(1200), WeightUnit.KG,
                BigDecimal.valueOf(12.5), VolumeUnit.CBM, null, false, null, false, null, null, true, null
        );
    }

    private RfqContainerRequest validContainerRequest() {
        return new RfqContainerRequest(RfqContainerType.DRY_20, 1, BigDecimal.valueOf(1200), WeightUnit.KG, null);
    }

    private RfqCargoItem validCargoEntity() {
        RfqCargoItem item = new RfqCargoItem("Electronics", 10, BigDecimal.valueOf(1200), WeightUnit.KG);
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        return item;
    }

    private RfqContainerRequirement validContainerEntity() {
        RfqContainerRequirement item = new RfqContainerRequirement(RfqContainerType.DRY_20, 1);
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        return item;
    }
}
