package com.freightflow.helpers;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Database cleaner integration")
class DatabaseCleanerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private DatabaseCleaner databaseCleaner;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PortRepository portRepository;
    @Autowired private RfqRepository rfqRepository;
    @Autowired private QuotationRepository quotationRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("deveLimparBancoEAceitarNovasOperacoesAposViolacaoEsperada")
    void deveLimparBancoEAceitarNovasOperacoesAposViolacaoEsperada() {
        Tenant tenantA = tenantRepository.save(new Tenant("Cleaner Tenant A", "cleaner-tenant-a", "cleaner-a@test.com", "FREE"));
        Tenant tenantB = tenantRepository.save(new Tenant("Cleaner Tenant B", "cleaner-tenant-b", "cleaner-b@test.com", "FREE"));

        User userA = userRepository.save(new User("Cleaner User A", "cleaner-user-a@test.com", "hash", User.UserRole.ADMIN, tenantA));
        User userB = userRepository.save(new User("Cleaner User B", "cleaner-user-b@test.com", "hash", User.UserRole.ADMIN, tenantB));

        Port origin = portRepository.save(new Port("CLN01", "Cleaner Santos", "BR", "America/Sao_Paulo", null, null));
        Port destination = portRepository.save(new Port("CLN02", "Cleaner Rotterdam", "NL", "Europe/Amsterdam", null, null));

        RequestForQuotation rfq = new RequestForQuotation(
                tenantA,
                "RFQ-CLEAN-1",
                "Maria",
                RfqDirection.EXPORT,
                RfqTransportMode.OCEAN,
                RfqServiceType.LCL,
                origin,
                destination,
                userA
        );
        rfq.setContactEmail("cleaner-maria@test.com");
        rfq.setProspectCompanyName("Cleaner Prospect");
        rfq.setStatus(RfqStatus.UNDER_ANALYSIS);
        rfq = rfqRepository.saveAndFlush(rfq);

        UUID tenantAId = tenantA.getId();
        UUID tenantBId = tenantB.getId();
        UUID userBId = userB.getId();
        UUID rfqId = rfq.getId();

        assertThatThrownBy(() -> executeInRequiresNew(() -> {
            Quotation invalidQuotation = new Quotation(
                    findTenant(tenantBId),
                    findRfq(rfqId),
                    "Q-CLEAN-1",
                    "USD",
                    findUser(userBId)
            );
            quotationRepository.saveAndFlush(invalidQuotation);
            return null;
        })).isInstanceOf(DataIntegrityViolationException.class);

        databaseCleaner.clean();

        assertThat(tenantRepository.count()).isZero();
        assertThat(rfqRepository.count()).isZero();
        assertThat(quotationRepository.count()).isZero();

        Tenant persistedAfterClean = tenantRepository.saveAndFlush(
                new Tenant("Post Clean Tenant", "post-clean-tenant", "post-clean@test.com", "FREE")
        );

        assertThat(persistedAfterClean.getId()).isNotNull();
        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(tenantAId).isNotEqualTo(tenantBId);
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
