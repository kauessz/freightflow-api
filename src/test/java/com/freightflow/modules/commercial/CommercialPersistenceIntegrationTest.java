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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

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
    @Autowired private PlatformTransactionManager transactionManager;

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

        UUID tenantAId = tenantA.getId();
        UUID tenantBId = tenantB.getId();
        UUID userAId = userA.getId();
        UUID userBId = userB.getId();
        UUID rfqId = rfq.getId();

        assertThatThrownBy(() -> executeInRequiresNew(() -> {
            Quotation invalidQuotation = new Quotation(
                    findTenant(tenantBId),
                    findRfq(rfqId),
                    "Q-X-1",
                    "USD",
                    findUser(userBId)
            );
            quotationRepository.saveAndFlush(invalidQuotation);
            return null;
        }))
                .isInstanceOf(DataIntegrityViolationException.class);

        UUID persistedQuotationId = executeInRequiresNew(() -> {
            Quotation validQuotation = new Quotation(
                    findTenant(tenantAId),
                    findRfq(rfqId),
                    "Q-X-2",
                    "USD",
                    findUser(userAId)
            );
            return quotationRepository.saveAndFlush(validQuotation).getId();
        });

        Quotation persistedQuotation = quotationRepository.findById(persistedQuotationId).orElseThrow();

        assertThat(persistedQuotation.getId()).isNotNull();
        assertThat(persistedQuotation.getTenant().getId()).isEqualTo(tenantAId);
        assertThat(persistedQuotation.getRfq().getId()).isEqualTo(rfqId);
    }

    private <T> T executeInRequiresNew(TransactionCallback<T> callback) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template.execute(status -> callback.execute());
    }

    private Tenant findTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId).orElseThrow();
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    private RequestForQuotation findRfq(UUID rfqId) {
        return rfqRepository.findById(rfqId).orElseThrow();
    }

    @FunctionalInterface
    private interface TransactionCallback<T> {
        T execute();
    }
}
