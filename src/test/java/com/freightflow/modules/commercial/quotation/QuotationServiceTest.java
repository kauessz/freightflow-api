package com.freightflow.modules.commercial.quotation;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.quotation.dto.CreateQuotationItemRequest;
import com.freightflow.modules.commercial.quotation.dto.CreateQuotationRequest;
import com.freightflow.modules.commercial.quotation.dto.UpdateQuotationItemRequest;
import com.freightflow.modules.commercial.quotation.enums.ChargeCategory;
import com.freightflow.modules.commercial.quotation.enums.ChargeScope;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.RfqRepository;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.port.Port;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuotationService")
class QuotationServiceTest {

    @Mock private QuotationRepository quotationRepository;
    @Mock private RfqRepository rfqRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private UserRepository userRepository;

    private QuotationService quotationService;

    private UUID tenantId;
    private UUID rfqId;
    private UUID userId;
    private Tenant tenant;
    private User user;
    private RequestForQuotation rfq;
    private Customer customer;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        rfqId = UUID.randomUUID();
        userId = UUID.randomUUID();

        tenant = new Tenant("Tenant", "tenant", "ops@test.com", "FREE");
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        user = new User("Operator", "op@test.com", "hash", User.UserRole.OPERATOR, tenant);
        ReflectionTestUtils.setField(user, "id", userId);
        customer = new Customer(tenant, "Atlas Cargo");
        ReflectionTestUtils.setField(customer, "id", UUID.randomUUID());
        Port origin = new Port("BRSSZ", "Santos", "BR", "America/Sao_Paulo", null, null);
        Port destination = new Port("NLRTM", "Rotterdam", "NL", "Europe/Amsterdam", null, null);
        rfq = new RequestForQuotation(tenant, "RFQ-1", "Maria", RfqDirection.EXPORT, RfqTransportMode.OCEAN, RfqServiceType.FCL, origin, destination, user);
        ReflectionTestUtils.setField(rfq, "id", rfqId);
        rfq.setCustomer(customer);
        rfq.setContactEmail("maria@test.com");
        rfq.setStatus(RfqStatus.UNDER_ANALYSIS);
        rfq.replaceCargoItems(List.of(new com.freightflow.modules.commercial.rfq.RfqCargoItem("Cargo", 1, BigDecimal.valueOf(100), com.freightflow.modules.commercial.shared.WeightUnit.KG)));
        rfq.replaceContainerRequirements(List.of(new com.freightflow.modules.commercial.rfq.RfqContainerRequirement(com.freightflow.modules.commercial.rfq.enums.RfqContainerType.DRY_20, 1)));
        quotationService = new QuotationService(
                quotationRepository,
                rfqRepository,
                tenantRepository,
                userRepository,
                new QuotationFinancialCalculator()
        );
    }

    @Test
    @DisplayName("deveCriarCotacaoValida")
    void deveCriarCotacaoValida() {
        CreateQuotationRequest request = validQuotationRequest();

        when(rfqRepository.findByIdAndTenantId(rfqId, tenantId)).thenReturn(Optional.of(rfq));
        when(quotationRepository.existsByTenantIdAndQuotationNumberAndRevision(tenantId, "Q-001", 1)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(invocation -> {
            Quotation quotation = invocation.getArgument(0);
            assignQuotationGraphIds(quotation);
            return quotation;
        });
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        var response = quotationService.create(rfqId, request, tenantId, userId);

        assertThat(response.quotationNumber()).isEqualTo("Q-001");
        assertThat(response.status()).isEqualTo(QuotationStatus.DRAFT);
        assertThat(response.sellingCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("deveRejeitarCriacaoCrossTenant")
    void deveRejeitarCriacaoCrossTenant() {
        when(rfqRepository.findByIdAndTenantId(rfqId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quotationService.create(rfqId, validQuotationRequest(), tenantId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("RequestForQuotation");
    }

    @Test
    @DisplayName("deveExigirRfqEmAnaliseParaCriarCotacao")
    void deveExigirRfqEmAnaliseParaCriarCotacao() {
        rfq.setStatus(RfqStatus.SUBMITTED);
        when(rfqRepository.findByIdAndTenantId(rfqId, tenantId)).thenReturn(Optional.of(rfq));

        assertThatThrownBy(() -> quotationService.create(rfqId, validQuotationRequest(), tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("UNDER_ANALYSIS");
    }

    @Test
    @DisplayName("deveRejeitarTransitTimeNegativo")
    void deveRejeitarTransitTimeNegativo() {
        rfq.setStatus(RfqStatus.UNDER_ANALYSIS);
        when(rfqRepository.findByIdAndTenantId(rfqId, tenantId)).thenReturn(Optional.of(rfq));
        when(quotationRepository.existsByTenantIdAndQuotationNumberAndRevision(tenantId, "Q-001", 1)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> quotationService.create(rfqId, new CreateQuotationRequest(
                "Q-001",
                Instant.parse("2026-07-30T10:00:00Z"),
                "MSC",
                -1,
                7,
                Instant.parse("2026-07-24T10:00:00Z"),
                Instant.parse("2026-08-10T10:00:00Z"),
                "USD",
                new BigDecimal("5.1234"),
                Instant.parse("2026-07-20T10:00:00Z"),
                "MANUAL",
                "Commercial notes",
                "Internal notes"
        ), tenantId, userId)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transit time days");
    }

    @Test
    @DisplayName("deveAdicionarItemERecalcularTotais")
    void deveAdicionarItemERecalcularTotais() {
        Quotation quotation = persistedDraftQuotation();
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(quotation)).thenAnswer(invocation -> {
            assignQuotationGraphIds(quotation);
            return quotation;
        });
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        var response = quotationService.addItem(quotation.getId(), validItemRequest(), tenantId);

        assertThat(response.items()).hasSize(1);
        assertThat(response.costTotal()).isEqualByComparingTo("100.00");
        assertThat(response.sellingTotal()).isEqualByComparingTo("150.00");
        assertThat(response.profitAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("deveExcluirItemOpcionalDoTotal")
    void deveExcluirItemOpcionalDoTotal() {
        Quotation quotation = persistedDraftQuotation();
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(quotation)).thenAnswer(invocation -> {
            assignQuotationGraphIds(quotation);
            return quotation;
        });
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        quotationService.addItem(quotation.getId(), validItemRequest(), tenantId);
        var optionalResponse = quotationService.addItem(quotation.getId(), new CreateQuotationItemRequest(
                ChargeCategory.DOCUMENTATION, "Optional docs", ChargeScope.ORIGIN, "USD",
                new BigDecimal("5"), null, "USD", new BigDecimal("8"), BigDecimal.ONE,
                null, true, true, null, null, 1
        ), tenantId);

        assertThat(optionalResponse.items()).hasSize(2);
        assertThat(optionalResponse.costTotal()).isEqualByComparingTo("100.00");
        assertThat(optionalResponse.sellingTotal()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("deveSuportarConversaoDeMoeda")
    void deveSuportarConversaoDeMoeda() {
        Quotation quotation = persistedDraftQuotation();
        quotation.setExchangeRate(new BigDecimal("5.4321"));
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(quotation)).thenAnswer(invocation -> {
            assignQuotationGraphIds(quotation);
            return quotation;
        });
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        var response = quotationService.addItem(quotation.getId(), new CreateQuotationItemRequest(
                ChargeCategory.OCEAN_FREIGHT, "Ocean freight", ChargeScope.MAIN_CARRIAGE, "BRL",
                new BigDecimal("1000"), null, "USD", new BigDecimal("250"), BigDecimal.ONE,
                null, true, false, null, null, 0
        ), tenantId);

        assertThat(response.items().get(0).exchangeRate()).isEqualByComparingTo("5.432100");
        assertThat(response.items().get(0).costAmountInSellingCurrency()).isEqualByComparingTo("5432.10");
    }

    @Test
    @DisplayName("deveBloquearAlteracaoForaDeDraft")
    void deveBloquearAlteracaoForaDeDraft() {
        Quotation quotation = persistedDraftQuotation();
        quotation.setStatus(QuotationStatus.READY_FOR_REVIEW);
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.update(quotation.getId(), new com.freightflow.modules.commercial.quotation.dto.UpdateQuotationRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, null
        ), tenantId)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    @DisplayName("deveMarcarReadyForReviewEAtualizarRfq")
    void deveMarcarReadyForReviewEAtualizarRfq() {
        Quotation quotation = persistedDraftQuotation();
        quotation.addItem(buildItem(quotation));
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(quotation)).thenAnswer(invocation -> {
            assignQuotationGraphIds(quotation);
            return quotation;
        });
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        var response = quotationService.readyForReview(quotation.getId(), tenantId);

        assertThat(response.status()).isEqualTo(QuotationStatus.READY_FOR_REVIEW);
        assertThat(quotation.getRfq().getStatus()).isEqualTo(RfqStatus.UNDER_ANALYSIS);
    }

    @Test
    @DisplayName("deveAprovarQuotationEmReadyForReview")
    void deveAprovarQuotationEmReadyForReview() {
        Quotation quotation = persistedDraftQuotation();
        quotation.setStatus(QuotationStatus.READY_FOR_REVIEW);
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(quotationRepository.save(quotation)).thenReturn(quotation);
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        var response = quotationService.approve(quotation.getId(), tenantId, userId);

        assertThat(response.status()).isEqualTo(QuotationStatus.APPROVED);
        assertThat(quotation.getApprovedAt()).isNotNull();
        assertThat(quotation.getApprovedBy()).isEqualTo(user);
    }

    @Test
    @DisplayName("deveEnviarQuotationAprovadaEMarcarRfqComoQuoted")
    void deveEnviarQuotationAprovadaEMarcarRfqComoQuoted() {
        Quotation quotation = persistedDraftQuotation();
        quotation.setStatus(QuotationStatus.APPROVED);
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(quotationRepository.save(quotation)).thenReturn(quotation);
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        var response = quotationService.send(quotation.getId(), tenantId, userId);

        assertThat(response.status()).isEqualTo(QuotationStatus.SENT);
        assertThat(quotation.getSentAt()).isNotNull();
        assertThat(quotation.getSentBy()).isEqualTo(user);
        assertThat(quotation.getRfq().getStatus()).isEqualTo(RfqStatus.QUOTED);
    }

    @Test
    @DisplayName("deveBloquearSendQuandoRfqEstaEmDraft")
    void deveBloquearSendQuandoRfqEstaEmDraft() {
        assertSendBlockedForRfqStatus(RfqStatus.DRAFT);
    }

    @Test
    @DisplayName("deveBloquearSendQuandoRfqEstaEmSubmitted")
    void deveBloquearSendQuandoRfqEstaEmSubmitted() {
        assertSendBlockedForRfqStatus(RfqStatus.SUBMITTED);
    }

    @Test
    @DisplayName("deveBloquearSendQuandoRfqEstaEmQuoted")
    void deveBloquearSendQuandoRfqEstaEmQuoted() {
        assertSendBlockedForRfqStatus(RfqStatus.QUOTED);
    }

    @Test
    @DisplayName("deveBloquearSendQuandoRfqEstaEmCancelled")
    void deveBloquearSendQuandoRfqEstaEmCancelled() {
        assertSendBlockedForRfqStatus(RfqStatus.CANCELLED);
    }

    @Test
    @DisplayName("deveBloquearSendQuandoRfqEstaEmExpired")
    void deveBloquearSendQuandoRfqEstaEmExpired() {
        assertSendBlockedForRfqStatus(RfqStatus.EXPIRED);
    }

    @Test
    @DisplayName("deveBloquearSendQuandoQuotationNaoEstaApproved")
    void deveBloquearSendQuandoQuotationNaoEstaApproved() {
        Quotation quotation = persistedDraftQuotation();
        quotation.setStatus(QuotationStatus.READY_FOR_REVIEW);
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.send(quotation.getId(), tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("APPROVED");

        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.READY_FOR_REVIEW);
        assertThat(quotation.getRfq().getStatus()).isEqualTo(RfqStatus.UNDER_ANALYSIS);
        verify(quotationRepository, never()).save(any(Quotation.class));
    }

    @Test
    @DisplayName("sentBloqueiaEdicaoDeCabecalhoEItensECancelamento")
    void sentBloqueiaEdicaoDeCabecalhoEItensECancelamento() {
        Quotation quotation = persistedDraftQuotation();
        QuotationItem item = buildItem(quotation);
        quotation.addItem(item);
        quotation.setStatus(QuotationStatus.SENT);
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.update(quotation.getId(), new com.freightflow.modules.commercial.quotation.dto.UpdateQuotationRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, null
        ), tenantId)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");

        assertThatThrownBy(() -> quotationService.addItem(quotation.getId(), validItemRequest(), tenantId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");

        assertThatThrownBy(() -> quotationService.updateItem(quotation.getId(), item.getId(), new UpdateQuotationItemRequest(
                null, null, null, null, null, null, null, new BigDecimal("200"),
                BigDecimal.ONE, null, null, null, null, null, null
        ), tenantId)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");

        assertThatThrownBy(() -> quotationService.deleteItem(quotation.getId(), item.getId(), tenantId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");

        assertThatThrownBy(() -> quotationService.cancel(quotation.getId(), tenantId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    @DisplayName("sentBloqueiaApproveNovamenteESendNovamente")
    void sentBloqueiaApproveNovamenteESendNovamente() {
        Quotation quotation = persistedDraftQuotation();
        quotation.setStatus(QuotationStatus.SENT);
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.approve(quotation.getId(), tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("READY_FOR_REVIEW");

        assertThatThrownBy(() -> quotationService.send(quotation.getId(), tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    @DisplayName("deveRejeitarSortOrderNegativo")
    void deveRejeitarSortOrderNegativo() {
        Quotation quotation = persistedDraftQuotation();
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.addItem(quotation.getId(), new CreateQuotationItemRequest(
                ChargeCategory.OTHER, "Charge", ChargeScope.GENERAL, "USD",
                new BigDecimal("10"), null, "USD", new BigDecimal("12"), BigDecimal.ONE,
                null, true, false, null, null, -1
        ), tenantId)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Sort order");

        verify(quotationRepository, never()).save(any(Quotation.class));
    }

    @Test
    @DisplayName("deveAtualizarERemoverItem")
    void deveAtualizarERemoverItem() {
        Quotation quotation = persistedDraftQuotation();
        QuotationItem item = buildItem(quotation);
        quotation.addItem(item);
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(quotation)).thenAnswer(invocation -> {
            assignQuotationGraphIds(quotation);
            return quotation;
        });
        when(quotationRepository.countByRfqIdsAndTenantId(List.of(rfqId), tenantId)).thenReturn(List.of());

        var updated = quotationService.updateItem(quotation.getId(), item.getId(), new UpdateQuotationItemRequest(
                null, null, null, null, null, null, null, new BigDecimal("200"),
                BigDecimal.ONE, null, null, null, null, null, null
        ), tenantId);
        assertThat(updated.sellingTotal()).isEqualByComparingTo("200.00");

        var afterDelete = quotationService.deleteItem(quotation.getId(), item.getId(), tenantId);
        assertThat(afterDelete.items()).isEmpty();
        assertThat(afterDelete.sellingTotal()).isEqualByComparingTo("0.00");
    }

    private CreateQuotationRequest validQuotationRequest() {
        return new CreateQuotationRequest(
                "Q-001",
                Instant.parse("2026-07-30T10:00:00Z"),
                "MSC",
                18,
                7,
                Instant.parse("2026-07-24T10:00:00Z"),
                Instant.parse("2026-08-10T10:00:00Z"),
                "usd",
                new BigDecimal("5.1234"),
                Instant.parse("2026-07-20T10:00:00Z"),
                "MANUAL",
                "Commercial notes",
                "Internal notes"
        );
    }

    private CreateQuotationItemRequest validItemRequest() {
        return new CreateQuotationItemRequest(
                ChargeCategory.OCEAN_FREIGHT,
                "Ocean freight",
                ChargeScope.MAIN_CARRIAGE,
                "USD",
                new BigDecimal("100"),
                null,
                "USD",
                new BigDecimal("150"),
                BigDecimal.ONE,
                "BL",
                true,
                false,
                "MSC",
                "Main charge",
                0
        );
    }

    private Quotation persistedDraftQuotation() {
        Quotation quotation = new Quotation(tenant, rfq, "Q-001", "USD", user);
        assignQuotationGraphIds(quotation);
        return quotation;
    }

    private void assertSendBlockedForRfqStatus(RfqStatus rfqStatus) {
        Quotation quotation = persistedDraftQuotation();
        quotation.setStatus(QuotationStatus.APPROVED);
        quotation.getRfq().setStatus(rfqStatus);
        when(quotationRepository.findByIdAndTenantId(quotation.getId(), tenantId)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.send(quotation.getId(), tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("UNDER_ANALYSIS");

        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.APPROVED);
        assertThat(quotation.getSentAt()).isNull();
        assertThat(quotation.getSentBy()).isNull();
        assertThat(quotation.getRfq().getStatus()).isEqualTo(rfqStatus);
        verify(quotationRepository, never()).save(any(Quotation.class));
    }

    private QuotationItem buildItem(Quotation quotation) {
        QuotationItem item = new QuotationItem(
                ChargeCategory.OCEAN_FREIGHT, "Ocean freight", ChargeScope.MAIN_CARRIAGE,
                "USD", new BigDecimal("100"), "USD", new BigDecimal("150"), BigDecimal.ONE, 0
        );
        item.setCostAmountInSellingCurrency(new BigDecimal("100"));
        item.setTotals(new BigDecimal("100"), new BigDecimal("150"), new BigDecimal("50"), new BigDecimal("33.3333"), new BigDecimal("50.0000"));
        item.setQuotation(quotation);
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        return item;
    }

    private void assignQuotationGraphIds(Quotation quotation) {
        if (quotation.getId() == null) {
            ReflectionTestUtils.setField(quotation, "id", UUID.randomUUID());
        }
        quotation.getItems().forEach(item -> {
            if (item.getId() == null) {
                ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
            }
        });
    }
}
