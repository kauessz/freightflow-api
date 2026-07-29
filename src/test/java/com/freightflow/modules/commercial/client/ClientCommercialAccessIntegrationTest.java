package com.freightflow.modules.commercial.client;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.commercial.quotation.enums.QuotationStatus;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.RfqRepository;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.shared.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Client commercial access integration")
class ClientCommercialAccessIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PortRepository portRepository;
    @Autowired private RfqRepository rfqRepository;
    @Autowired private QuotationRepository quotationRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private Tenant tenant;
    private Customer customerA;
    private Customer customerB;
    private User clientA;
    private User clientB;
    private RequestForQuotation rfqA;
    private Quotation sentQuotationA;
    private Quotation approvedQuotationA;

    @BeforeEach
    void setUpData() {
        tenant = tenantRepository.save(new Tenant("Portal Tenant", "portal-tenant", "portal@test.com", "FREE"));

        customerA = new Customer(tenant, "Atlas Cargo");
        ReflectionTestUtils.setField(customerA, "id", UUID.randomUUID());
        entityManager.persist(customerA);

        customerB = new Customer(tenant, "Meridian Imports");
        ReflectionTestUtils.setField(customerB, "id", UUID.randomUUID());
        entityManager.persist(customerB);

        clientA = new User("Client A", "client-a@test.com", "hash", User.UserRole.CLIENT, tenant);
        clientA.setCustomer(customerA);
        clientA = userRepository.save(clientA);

        clientB = new User("Client B", "client-b@test.com", "hash", User.UserRole.CLIENT, tenant);
        clientB.setCustomer(customerB);
        clientB = userRepository.save(clientB);

        User admin = userRepository.save(new User("Admin", "admin@test.com", "hash", User.UserRole.ADMIN, tenant));

        Port origin = portRepository.save(new Port("BRSSZ", "Santos", "BR", "America/Sao_Paulo", null, null));
        Port destination = portRepository.save(new Port("NLRTM", "Rotterdam", "NL", "Europe/Amsterdam", null, null));

        rfqA = new RequestForQuotation(
                tenant,
                "RFQ-PORTAL-1",
                "Maria",
                RfqDirection.EXPORT,
                RfqTransportMode.OCEAN,
                RfqServiceType.LCL,
                origin,
                destination,
                clientA
        );
        rfqA.setCustomer(customerA);
        rfqA.setContactEmail("maria@atlas.com");
        rfqA.setStatus(RfqStatus.QUOTED);
        rfqA.setProspectCompanyName(null);
        rfqA = rfqRepository.saveAndFlush(rfqA);

        sentQuotationA = new Quotation(tenant, rfqA, "Q-PORTAL-SENT", "USD", admin);
        sentQuotationA.setStatus(QuotationStatus.SENT);
        sentQuotationA.setSentAt(java.time.Instant.parse("2026-07-29T10:00:00Z"));
        sentQuotationA.setCommercialNotes("Visible note");
        sentQuotationA = quotationRepository.saveAndFlush(sentQuotationA);

        approvedQuotationA = new Quotation(tenant, rfqA, "Q-PORTAL-APPROVED", "USD", admin);
        approvedQuotationA.setStatus(QuotationStatus.APPROVED);
        approvedQuotationA = quotationRepository.saveAndFlush(approvedQuotationA);
    }

    @Test
    @DisplayName("rfqDeOutroCustomerRetorna404")
    void rfqDeOutroCustomerRetorna404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/rfqs/{id}", rfqA.getId())
                        .with(user(principal(clientB))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("quotationSentDoCustomerCorretoApareceMasNaoParaOutroCustomer")
    void quotationSentDoCustomerCorretoApareceMasNaoParaOutroCustomer() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations")
                        .with(user(principal(clientA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].quotationNumber").value("Q-PORTAL-SENT"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations/{id}", sentQuotationA.getId())
                        .with(user(principal(clientB))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("quotationNaoEnviadaNaoApareceParaCliente")
    void quotationNaoEnviadaNaoApareceParaCliente() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/client/quotations/{id}", approvedQuotationA.getId())
                        .with(user(principal(clientA)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private UserPrincipal principal(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                null,
                tenant.getId(),
                user.getRole().name(),
                user.getCustomer() != null ? user.getCustomer().getId() : null
        );
    }
}
