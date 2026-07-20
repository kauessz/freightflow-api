package com.freightflow.modules.commercial;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.RfqRepository;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Commercial persistence integration")
@Testcontainers(disabledWithoutDocker = true)
class CommercialPersistenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PortRepository portRepository;
    @Autowired private RfqRepository rfqRepository;
    @Autowired private QuotationRepository quotationRepository;

    @Test
    @DisplayName("deveAplicarFkCompostaPorTenantNaPersistenciaDeQuotation")
    void deveAplicarFkCompostaPorTenantNaPersistenciaDeQuotation() {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant A", "tenant-a", "a@test.com", "FREE"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant B", "tenant-b", "b@test.com", "FREE"));

        User userA = userRepository.save(new User("User A", "user-a@test.com", "hash", User.UserRole.ADMIN, tenantA));
        User userB = userRepository.save(new User("User B", "user-b@test.com", "hash", User.UserRole.ADMIN, tenantB));

        Port origin = portRepository.save(new Port("BRS01", "Santos Test", "BR", "America/Sao_Paulo", null, null));
        Port destination = portRepository.save(new Port("NLR01", "Rotterdam Test", "NL", "Europe/Amsterdam", null, null));

        RequestForQuotation rfq = new RequestForQuotation(
                tenantA,
                "RFQ-X-1",
                "Maria",
                RfqDirection.EXPORT,
                RfqTransportMode.OCEAN,
                RfqServiceType.LCL,
                origin,
                destination,
                userA
        );
        rfq.setContactEmail("maria@test.com");
        rfq.setProspectCompanyName("Prospect A");
        rfq.setStatus(RfqStatus.UNDER_ANALYSIS);
        rfq = rfqRepository.saveAndFlush(rfq);

        Quotation quotation = new Quotation(tenantB, rfq, "Q-X-1", "USD", userB);

        assertThatThrownBy(() -> quotationRepository.saveAndFlush(quotation))
                .isInstanceOf(DataIntegrityViolationException.class);

        Quotation validQuotation = new Quotation(tenantA, rfq, "Q-X-2", "USD", userA);
        Quotation persistedQuotation = quotationRepository.saveAndFlush(validQuotation);

        assertThat(persistedQuotation.getId()).isNotNull();
        assertThat(persistedQuotation.getTenant().getId()).isEqualTo(tenantA.getId());
        assertThat(persistedQuotation.getRfq().getId()).isEqualTo(rfq.getId());
    }
}
