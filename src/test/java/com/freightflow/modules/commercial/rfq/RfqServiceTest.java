package com.freightflow.modules.commercial.rfq;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.commercial.rfq.dto.CreateRfqRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqCargoItemRequest;
import com.freightflow.modules.commercial.rfq.dto.RfqContainerRequest;
import com.freightflow.modules.commercial.rfq.dto.UpdateRfqRequest;
import com.freightflow.modules.commercial.rfq.enums.RfqContainerType;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.shared.IncotermCode;
import com.freightflow.modules.commercial.shared.WeightUnit;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.customer.CustomerRepository;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@DisplayName("RfqService")
class RfqServiceTest {

    @Mock private RfqRepository rfqRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private PortRepository portRepository;
    @Mock private QuotationRepository quotationRepository;

    @InjectMocks private RfqService rfqService;

    private UUID tenantId;
    private UUID otherTenantId;
    private UUID userId;
    private UUID customerId;
    private UUID originPortId;
    private UUID destinationPortId;
    private Tenant tenant;
    private Tenant otherTenant;
    private User user;
    private User otherTenantUser;
    private Customer customer;
    private Port originPort;
    private Port destinationPort;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        otherTenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        originPortId = UUID.randomUUID();
        destinationPortId = UUID.randomUUID();

        tenant = new Tenant("Tenant A", "tenant-a", "ops@tenant-a.com", "FREE");
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        otherTenant = new Tenant("Tenant B", "tenant-b", "ops@tenant-b.com", "FREE");
        ReflectionTestUtils.setField(otherTenant, "id", otherTenantId);

        user = new User("Operator A", "op@tenant-a.com", "hash", User.UserRole.OPERATOR, tenant);
        ReflectionTestUtils.setField(user, "id", userId);

        otherTenantUser = new User("Operator B", "op@tenant-b.com", "hash", User.UserRole.OPERATOR, otherTenant);
        ReflectionTestUtils.setField(otherTenantUser, "id", UUID.randomUUID());

        customer = new Customer(tenant, "Atlas Cargo");
        ReflectionTestUtils.setField(customer, "id", customerId);

        originPort = new Port("BRSSZ", "Santos", "BR", "America/Sao_Paulo", null, null);
        ReflectionTestUtils.setField(originPort, "id", originPortId);
        destinationPort = new Port("NLRTM", "Rotterdam", "NL", "Europe/Amsterdam", null, null);
        ReflectionTestUtils.setField(destinationPort, "id", destinationPortId);
    }

    @Test
    @DisplayName("deveCriarRfqValida")
    void deveCriarRfqValida() {
        CreateRfqRequest request = validFclRequest();

        when(rfqRepository.existsByReferenceAndTenantId("RFQ-001", tenantId)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(customerRepository.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(portRepository.findById(originPortId)).thenReturn(Optional.of(originPort));
        when(portRepository.findById(destinationPortId)).thenReturn(Optional.of(destinationPort));
        when(rfqRepository.save(any(RequestForQuotation.class))).thenAnswer(invocation -> {
            RequestForQuotation saved = invocation.getArgument(0);
            assignGraphIds(saved);
            return saved;
        });

        var response = rfqService.create(request, tenantId, userId);

        assertThat(response.reference()).isEqualTo("RFQ-001");
        assertThat(response.customerName()).isEqualTo("Atlas Cargo");
        assertThat(response.status()).isEqualTo(RfqStatus.DRAFT);
        assertThat(response.cargoItems()).hasSize(1);
        assertThat(response.containers()).hasSize(1);
    }

    @Test
    @DisplayName("deveRejeitarReferenciaDuplicadaNoMesmoTenant")
    void deveRejeitarReferenciaDuplicadaNoMesmoTenant() {
        when(rfqRepository.existsByReferenceAndTenantId("RFQ-001", tenantId)).thenReturn(true);

        assertThatThrownBy(() -> rfqService.create(validFclRequest(), tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("devePermitirMesmaReferenciaEmTenantDiferente")
    void devePermitirMesmaReferenciaEmTenantDiferente() {
        CreateRfqRequest request = validFclRequest();

        when(rfqRepository.existsByReferenceAndTenantId("RFQ-001", otherTenantId)).thenReturn(false);
        when(tenantRepository.findById(otherTenantId)).thenReturn(Optional.of(otherTenant));
        when(userRepository.findByIdAndTenantId(otherTenantUser.getId(), otherTenantId)).thenReturn(Optional.of(otherTenantUser));
        when(portRepository.findById(originPortId)).thenReturn(Optional.of(originPort));
        when(portRepository.findById(destinationPortId)).thenReturn(Optional.of(destinationPort));
        when(rfqRepository.save(any(RequestForQuotation.class))).thenAnswer(invocation -> {
            RequestForQuotation saved = invocation.getArgument(0);
            assignGraphIds(saved);
            return saved;
        });

        var response = rfqService.create(withCustomer(request, null), otherTenantId, otherTenantUser.getId());

        assertThat(response.reference()).isEqualTo("RFQ-001");
    }

    @Test
    @DisplayName("deveExigirCustomerOuProspect")
    void deveExigirCustomerOuProspect() {
        CreateRfqRequest request = new CreateRfqRequest(
                "RFQ-001", null, null, "Maria", "maria@test.com", null,
                RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.LCL,
                null, null, null, originPortId, destinationPortId,
                null, null, null, null, null, null,
                List.of(validCargo()), List.of()
        );

        when(rfqRepository.existsByReferenceAndTenantId("RFQ-001", tenantId)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(portRepository.findById(originPortId)).thenReturn(Optional.of(originPort));
        when(portRepository.findById(destinationPortId)).thenReturn(Optional.of(destinationPort));

        assertThatThrownBy(() -> rfqService.create(request, tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("customer or prospect");
    }

    @Test
    @DisplayName("deveExigirContatoMinimo")
    void deveExigirContatoMinimo() {
        CreateRfqRequest request = new CreateRfqRequest(
                "RFQ-001", customerId, null, "Maria", null, null,
                RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.LCL,
                null, null, null, originPortId, destinationPortId,
                null, null, null, null, null, null,
                List.of(validCargo()), List.of()
        );

        mockBaseCreateLookups();

        assertThatThrownBy(() -> rfqService.create(request, tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("contact email or phone");
    }

    @Test
    @DisplayName("deveRejeitarOrigemIgualDestino")
    void deveRejeitarOrigemIgualDestino() {
        CreateRfqRequest request = new CreateRfqRequest(
                "RFQ-001", customerId, null, "Maria", "maria@test.com", null,
                RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.LCL,
                null, null, null, originPortId, originPortId,
                null, null, null, null, null, null,
                List.of(validCargo()), List.of()
        );

        when(rfqRepository.existsByReferenceAndTenantId("RFQ-001", tenantId)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(customerRepository.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(portRepository.findById(originPortId)).thenReturn(Optional.of(originPort));

        assertThatThrownBy(() -> rfqService.create(request, tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("deveValidarIncotermMaritimo")
    void deveValidarIncotermMaritimo() {
        CreateRfqRequest request = new CreateRfqRequest(
                "RFQ-001", customerId, null, "Maria", "maria@test.com", null,
                RfqDirection.EXPORT, RfqTransportMode.AIR, RfqServiceType.LCL,
                IncotermCode.FOB, "2020", "Santos", originPortId, destinationPortId,
                null, null, null, null, null, null,
                List.of(validCargo()), List.of()
        );

        mockBaseCreateLookups();

        assertThatThrownBy(() -> rfqService.create(request, tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ocean context");
    }

    @Test
    @DisplayName("deveAceitarIncotermMaritimoEmModalOceanico")
    void deveAceitarIncotermMaritimoEmModalOceanico() {
        CreateRfqRequest request = validFclRequest();

        when(rfqRepository.existsByReferenceAndTenantId("RFQ-001", tenantId)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(customerRepository.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(portRepository.findById(originPortId)).thenReturn(Optional.of(originPort));
        when(portRepository.findById(destinationPortId)).thenReturn(Optional.of(destinationPort));
        when(rfqRepository.save(any(RequestForQuotation.class))).thenAnswer(invocation -> {
            RequestForQuotation saved = invocation.getArgument(0);
            assignGraphIds(saved);
            return saved;
        });

        var response = rfqService.create(request, tenantId, userId);

        assertThat(response.incotermCode()).isEqualTo(IncotermCode.FOB);
        assertThat(response.incotermVersion()).isEqualTo("2020");
    }

    @Test
    @DisplayName("deveRejeitarVersaoIncotermInvalida")
    void deveRejeitarVersaoIncotermInvalida() {
        CreateRfqRequest request = new CreateRfqRequest(
                "RFQ-001", customerId, null, "Maria", "maria@test.com", null,
                RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.LCL,
                IncotermCode.FCA, "2010", "Santos", originPortId, destinationPortId,
                null, null, null, null, null, null,
                List.of(validCargo()), List.of()
        );

        mockBaseCreateLookups();

        assertThatThrownBy(() -> rfqService.create(request, tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Incoterms 2020");
    }

    @Test
    @DisplayName("deveExigirContainerParaFcl")
    void deveExigirContainerParaFcl() {
        CreateRfqRequest request = new CreateRfqRequest(
                "RFQ-001", customerId, null, "Maria", "maria@test.com", null,
                RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.FCL,
                null, null, null, originPortId, destinationPortId,
                null, null, null, null, null, null,
                List.of(validCargo()), List.of()
        );

        mockBaseCreateLookups();

        assertThatThrownBy(() -> rfqService.create(request, tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("requires at least one container");
    }

    @Test
    @DisplayName("deveRejeitarContainerEmLcl")
    void deveRejeitarContainerEmLcl() {
        CreateRfqRequest request = new CreateRfqRequest(
                "RFQ-001", customerId, null, "Maria", "maria@test.com", null,
                RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.LCL,
                null, null, null, originPortId, destinationPortId,
                null, null, null, null, null, null,
                List.of(validCargo()), List.of(validContainer())
        );

        mockBaseCreateLookups();

        assertThatThrownBy(() -> rfqService.create(request, tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("only allowed for FCL");
    }

    @Test
    @DisplayName("deveRejeitarCustomerDeOutroTenant")
    void deveRejeitarCustomerDeOutroTenant() {
        CreateRfqRequest request = validFclRequest();

        when(rfqRepository.existsByReferenceAndTenantId("RFQ-001", tenantId)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(customerRepository.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.empty());
        when(portRepository.findById(originPortId)).thenReturn(Optional.of(originPort));
        when(portRepository.findById(destinationPortId)).thenReturn(Optional.of(destinationPort));

        assertThatThrownBy(() -> rfqService.create(request, tenantId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer");
    }

    @Test
    @DisplayName("deveRejeitarResponsavelDeOutroTenant")
    void deveRejeitarResponsavelDeOutroTenant() {
        CreateRfqRequest request = new CreateRfqRequest(
                "RFQ-001", customerId, null, "Maria", "maria@test.com", null,
                RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.FCL,
                null, null, null, originPortId, destinationPortId,
                null, null, null, null, otherTenantUser.getId(), null,
                List.of(validCargo()), List.of(validContainer())
        );

        when(rfqRepository.existsByReferenceAndTenantId("RFQ-001", tenantId)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(customerRepository.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(userRepository.findByIdAndTenantId(otherTenantUser.getId(), tenantId)).thenReturn(Optional.empty());
        when(portRepository.findById(originPortId)).thenReturn(Optional.of(originPort));
        when(portRepository.findById(destinationPortId)).thenReturn(Optional.of(destinationPort));

        assertThatThrownBy(() -> rfqService.create(request, tenantId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("deveSubmeterRascunhoValido")
    void deveSubmeterRascunhoValido() {
        RequestForQuotation rfq = buildPersistedDraftRfq();
        when(rfqRepository.findByIdAndTenantId(rfq.getId(), tenantId)).thenReturn(Optional.of(rfq));
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfq.getId()), tenantId)).thenReturn(List.of());
        when(rfqRepository.save(rfq)).thenReturn(rfq);

        var response = rfqService.submit(rfq.getId(), tenantId);

        assertThat(response.status()).isEqualTo(RfqStatus.SUBMITTED);
        assertThat(response.submittedAt()).isNotNull();
    }

    @Test
    @DisplayName("naoDeveExcluirRfqComQuotationAssociada")
    void naoDeveExcluirRfqComQuotationAssociada() {
        RequestForQuotation rfq = buildPersistedDraftRfq();
        when(rfqRepository.findByIdAndTenantId(rfq.getId(), tenantId)).thenReturn(Optional.of(rfq));
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfq.getId()), tenantId))
                .thenReturn(List.of(rfqQuotationCount(rfq.getId(), 1L)));

        assertThatThrownBy(() -> rfqService.delete(rfq.getId(), tenantId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete RFQ with quotations");

        verify(rfqRepository, never()).delete(any(RequestForQuotation.class));
    }

    @Test
    @DisplayName("deveRejeitarTransicaoInvalidaDeAnalise")
    void deveRejeitarTransicaoInvalidaDeAnalise() {
        RequestForQuotation rfq = buildPersistedDraftRfq();
        when(rfqRepository.findByIdAndTenantId(rfq.getId(), tenantId)).thenReturn(Optional.of(rfq));

        assertThatThrownBy(() -> rfqService.startAnalysis(rfq.getId(), tenantId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SUBMITTED");
    }

    @Test
    @DisplayName("naoDeveConsultarRfqDeOutroTenant")
    void naoDeveConsultarRfqDeOutroTenant() {
        UUID rfqId = UUID.randomUUID();
        when(rfqRepository.findByIdAndTenantId(rfqId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rfqService.getById(rfqId, tenantId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("RequestForQuotation");
    }

    @Test
    @DisplayName("naoDeveAtualizarRfqDeOutroTenant")
    void naoDeveAtualizarRfqDeOutroTenant() {
        UUID rfqId = UUID.randomUUID();
        when(rfqRepository.findByIdAndTenantId(rfqId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rfqService.update(rfqId, new UpdateRfqRequest(null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
                tenantId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(rfqRepository, never()).save(any());
    }

    private void mockBaseCreateLookups() {
        when(rfqRepository.existsByReferenceAndTenantId("RFQ-001", tenantId)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(customerRepository.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(portRepository.findById(originPortId)).thenReturn(Optional.of(originPort));
        when(portRepository.findById(destinationPortId)).thenReturn(Optional.of(destinationPort));
    }

    private RequestForQuotation buildPersistedDraftRfq() {
        RequestForQuotation rfq = new RequestForQuotation(
                tenant, "RFQ-001", "Maria", RfqDirection.EXPORT, RfqTransportMode.OCEAN,
                RfqServiceType.FCL, originPort, destinationPort, user
        );
        rfq.setCustomer(customer);
        rfq.setContactEmail("maria@test.com");
        rfq.replaceCargoItems(List.of(new RfqCargoItem("Cargo", 1, BigDecimal.valueOf(100), WeightUnit.KG)));
        rfq.replaceContainerRequirements(List.of(new RfqContainerRequirement(RfqContainerType.DRY_20, 1)));
        assignGraphIds(rfq);
        return rfq;
    }

    private void assignGraphIds(RequestForQuotation rfq) {
        if (rfq.getId() == null) {
            ReflectionTestUtils.setField(rfq, "id", UUID.randomUUID());
        }
        rfq.getCargoItems().forEach(item -> {
            if (item.getId() == null) {
                ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
            }
        });
        rfq.getContainerRequirements().forEach(item -> {
            if (item.getId() == null) {
                ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
            }
        });
    }

    private QuotationRepository.RfqQuotationCountView rfqQuotationCount(UUID rfqId, long quotationCount) {
        return new QuotationRepository.RfqQuotationCountView() {
            @Override
            public UUID getRfqId() {
                return rfqId;
            }

            @Override
            public long getQuotationCount() {
                return quotationCount;
            }
        };
    }

    private CreateRfqRequest validFclRequest() {
        return new CreateRfqRequest(
                "RFQ-001",
                customerId,
                null,
                "Maria Operadora",
                "maria@atlascargo.com",
                null,
                RfqDirection.EXPORT,
                RfqTransportMode.OCEAN,
                RfqServiceType.FCL,
                IncotermCode.FOB,
                "2020",
                "Santos",
                originPortId,
                destinationPortId,
                null,
                null,
                Instant.parse("2026-07-21T10:00:00Z"),
                Instant.parse("2026-07-22T10:00:00Z"),
                null,
                "Needs fast response",
                List.of(validCargo()),
                List.of(validContainer())
        );
    }

    private CreateRfqRequest withCustomer(CreateRfqRequest base, UUID newCustomerId) {
        return new CreateRfqRequest(
                base.reference(), newCustomerId, "Prospect Ocean", base.contactName(), base.contactEmail(), base.contactPhone(),
                base.direction(), base.transportMode(), base.serviceType(), base.incotermCode(), base.incotermVersion(),
                base.incotermNamedPlace(), base.originPortId(), base.destinationPortId(), base.placeOfReceipt(),
                base.placeOfDelivery(), base.cargoReadyDate(), base.desiredDepartureDate(), base.assignedTo(), base.notes(),
                base.cargoItems(), base.containers()
        );
    }

    private RfqCargoItemRequest validCargo() {
        return new RfqCargoItemRequest(
                "Electronics",
                "PALLET",
                10,
                BigDecimal.valueOf(1200),
                WeightUnit.KG,
                BigDecimal.valueOf(12.5),
                com.freightflow.modules.commercial.shared.VolumeUnit.CBM,
                null,
                false,
                null,
                false,
                null,
                null,
                true,
                null
        );
    }

    private RfqContainerRequest validContainer() {
        return new RfqContainerRequest(
                RfqContainerType.DRY_20,
                1,
                BigDecimal.valueOf(1200),
                WeightUnit.KG,
                null
        );
    }
}
