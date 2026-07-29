package com.freightflow.modules.commercial.client.quotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.commercial.client.quotation.dto.ClientQuotationResponse;
import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.QuotationItem;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.commercial.quotation.enums.ChargeCategory;
import com.freightflow.modules.commercial.quotation.enums.ChargeScope;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.port.Port;
import com.freightflow.shared.exception.ForbiddenException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientQuotationService")
class ClientQuotationServiceTest {

    @Mock private QuotationRepository quotationRepository;

    @InjectMocks private ClientQuotationService clientQuotationService;

    private UUID tenantId;
    private UUID customerId;
    private UUID quotationId;
    private Quotation quotation;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        quotationId = UUID.randomUUID();

        Tenant tenant = new Tenant("Tenant", "tenant", "ops@test.com", "FREE");
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        Customer customer = new Customer(tenant, "Atlas Cargo");
        ReflectionTestUtils.setField(customer, "id", customerId);
        User admin = new User("Admin", "admin@test.com", "hash", User.UserRole.ADMIN, tenant);
        ReflectionTestUtils.setField(admin, "id", UUID.randomUUID());
        Port origin = new Port("BRSSZ", "Santos", "BR", "America/Sao_Paulo", null, null);
        Port destination = new Port("NLRTM", "Rotterdam", "NL", "Europe/Amsterdam", null, null);

        RequestForQuotation rfq = new RequestForQuotation(
                tenant, "RFQ-1", "Maria", RfqDirection.EXPORT, RfqTransportMode.OCEAN,
                RfqServiceType.FCL, origin, destination, admin
        );
        ReflectionTestUtils.setField(rfq, "id", UUID.randomUUID());
        rfq.setCustomer(customer);
        rfq.setContactEmail("maria@test.com");

        quotation = new Quotation(tenant, rfq, "Q-1", "USD", admin);
        ReflectionTestUtils.setField(quotation, "id", quotationId);
        quotation.setStatus(QuotationStatus.SENT);
        quotation.setSentAt(java.time.Instant.parse("2026-07-29T10:00:00Z"));
        quotation.setCarrierName("MSC");
        quotation.setCommercialNotes("Visible note");
        quotation.setTotals(
                BigDecimal.ZERO,
                new BigDecimal("150.00"),
                new BigDecimal("50.00"),
                new BigDecimal("33.3333"),
                new BigDecimal("50.0000")
        );

        QuotationItem item = new QuotationItem(
                ChargeCategory.OCEAN_FREIGHT,
                "Ocean freight",
                ChargeScope.MAIN_CARRIAGE,
                "USD",
                new BigDecimal("100.00"),
                "USD",
                new BigDecimal("150.00"),
                BigDecimal.ONE,
                0
        );
        item.setCostAmountInSellingCurrency(new BigDecimal("100.00"));
        item.setSupplierName("Hidden Supplier");
        item.setNotes("Client visible line note");
        item.setTotals(
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                new BigDecimal("50.00"),
                new BigDecimal("33.3333"),
                new BigDecimal("50.0000")
        );
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        quotation.addItem(item);
    }

    @Test
    @DisplayName("approvedNaoApareceParaClient")
    void approvedNaoApareceParaClient() {
        when(quotationRepository.findByIdAndTenantIdAndRfqCustomerIdAndStatus(
                quotationId, tenantId, customerId, QuotationStatus.SENT
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientQuotationService.getById(quotationId, tenantId, customerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("sentApareceSomenteParaCustomerCorreto")
    void sentApareceSomenteParaCustomerCorreto() {
        when(quotationRepository.findByTenantIdAndRfqCustomerIdAndStatus(
                tenantId, customerId, QuotationStatus.SENT, PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(List.of(quotation), PageRequest.of(0, 20), 1));
        when(quotationRepository.findByIdAndTenantIdAndRfqCustomerIdAndStatus(
                quotationId, tenantId, customerId, QuotationStatus.SENT
        )).thenReturn(Optional.of(quotation));

        var list = clientQuotationService.list(tenantId, customerId, PageRequest.of(0, 20));
        var detail = clientQuotationService.getById(quotationId, tenantId, customerId);

        assertThat(list.data()).hasSize(1);
        assertThat(detail.id()).isEqualTo(quotationId.toString());
        assertThat(detail.sellingTotal()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("clientSemCustomerEhRejeitado")
    void clientSemCustomerEhRejeitado() {
        assertThatThrownBy(() -> clientQuotationService.list(tenantId, null, PageRequest.of(0, 20)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("serializacaoNaoVazaCamposInternos")
    void serializacaoNaoVazaCamposInternos() throws Exception {
        ClientQuotationResponse response = ClientQuotationResponse.from(quotation);

        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        String json = objectMapper.writeValueAsString(response);

        assertThat(json).doesNotContain("\"costCurrency\"");
        assertThat(json).doesNotContain("\"costAmount\"");
        assertThat(json).doesNotContain("\"exchangeRate\"");
        assertThat(json).doesNotContain("\"totalCost\"");
        assertThat(json).doesNotContain("\"costTotal\"");
        assertThat(json).doesNotContain("\"profitAmount\"");
        assertThat(json).doesNotContain("\"marginPercentage\"");
        assertThat(json).doesNotContain("\"markupPercentage\"");
        assertThat(json).doesNotContain("\"supplierName\"");
        assertThat(json).doesNotContain("\"internalNotes\"");
    }
}
